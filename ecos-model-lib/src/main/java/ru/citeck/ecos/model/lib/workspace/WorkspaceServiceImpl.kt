package ru.citeck.ecos.model.lib.workspace

import com.github.benmanes.caffeine.cache.Caffeine
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.model.lib.workspace.IdInWs.Companion.create
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.txn.lib.TxnContext
import java.time.Duration

class WorkspaceServiceImpl(services: ModelServiceFactory) : WorkspaceService {

    companion object {
        private const val USER_WORKSPACES_CACHE_KEY = "user-workspaces-txn-cache-key"
    }

    private val workspaceApi = services.workspaceApi

    private val nestedWorkspacesCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(30))
        .maximumSize(1000)
        .build<String, Set<String>> {
            workspaceApi.getNestedWorkspaces(listOf(it))[0]
        }

    private val workspaceSysIdCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(1000)
        .build<String, String> {
            workspaceApi.mapIdentifiers(listOf(it), WorkspaceApi.IdMappingType.WS_ID_TO_SYS_ID)[0]
        }

    private val workspaceIdBySysIdCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(1000)
        .build<String, String> {
            workspaceApi.mapIdentifiers(listOf(it), WorkspaceApi.IdMappingType.WS_SYS_ID_TO_ID)[0]
        }

    override fun expandWorkspaces(workspaces: Collection<String>): Set<String> {
        if (workspaces.isEmpty()) {
            return emptySet()
        }
        val resultWorkspaces = LinkedHashSet<String>(workspaces)
        for (workspace in workspaces) {
            if (workspace.startsWith(USER_WORKSPACE_PREFIX)) {
                continue
            }
            resultWorkspaces.addAll(nestedWorkspacesCache.get(workspace))
        }
        return resultWorkspaces
    }

    override fun getNestedWorkspaces(workspaces: List<String>): List<Set<String>> {
        if (workspaces.isEmpty()) {
            return emptyList()
        }
        return workspaces.map {
            if (it.startsWith(USER_WORKSPACE_PREFIX)) {
                emptySet()
            } else {
                nestedWorkspacesCache.get(it)
            }
        }
    }

    override fun getUserWorkspaces(user: String): Set<String> {
        return getUserWorkspaces(user, WsMembershipType.ALL)
    }

    override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> {

        if (getUserWorkspaceNotAllowed(user)) {
            return emptySet()
        }

        val userWorkspaces = TxnContext.getTxnOrNull()
            ?.getData(USER_WORKSPACES_CACHE_KEY) { HashMap<Pair<String, WsMembershipType>, Set<String>>() }
            ?.computeIfAbsent(user to membershipType) { getWorkspacesByApi(it.first, it.second) }
            ?: getWorkspacesByApi(user, membershipType)

        return if (membershipType == WsMembershipType.ALL) {
            val result = LinkedHashSet<String>(userWorkspaces)
            result.add("$USER_WORKSPACE_PREFIX$user")
            result
        } else {
            userWorkspaces
        }
    }

    private fun getUserWorkspaceNotAllowed(user: String): Boolean {
        if (AuthContext.isRunAsSystemOrAdmin()) {
            return false
        }

        val currentUser = AuthContext.getCurrentUser()
        return currentUser.isBlank() || currentUser != user
    }

    override fun isUserManagerOf(user: String, workspace: String): Boolean {
        if (isOwnPersonalWorkspace(user, workspace)) {
            return true
        }
        return AuthContext.runAsSystem {
            workspaceApi.isUserManagerOf(user, workspace)
        }
    }

    override fun isUserMemberOf(user: String, workspace: String): Boolean {
        if (isOwnPersonalWorkspace(user, workspace)) {
            return true
        }
        return getUserWorkspaces(user).contains(workspace)
    }

    override fun isWorkspaceWithGlobalArtifacts(workspace: String?): Boolean {
        return workspace.isNullOrBlank() ||
            workspace == ModelUtils.DEFAULT_WORKSPACE_ID ||
            workspace.startsWith("admin$")
    }

    override fun resetNestedWorkspacesCache() {
        nestedWorkspacesCache.invalidateAll()
    }

    override fun resetNestedWorkspacesCache(workspaces: Collection<String>) {
        for (workspace in workspaces) {
            nestedWorkspacesCache.invalidate(workspace)
        }
    }

    override fun getWorkspaceSystemId(workspace: String): String {
        return workspaceSysIdCache.get(workspace)
    }

    override fun getWorkspaceSystemId(workspaces: List<String>): List<String> {
        return workspaces.map { getWorkspaceSystemId(it) }
    }

    override fun getWorkspaceIdBySystemId(workspaceSysId: List<String>): List<String> {
        return workspaceSysId.map { getWorkspaceIdBySystemId(it) }
    }

    override fun getWorkspaceIdBySystemId(workspaceSysId: String): String {
        return workspaceIdBySysIdCache.get(workspaceSysId)
    }

    override fun removeWsPrefixFromId(id: String, workspace: String): String {
        val prefix = getPrefixForIdInWorkspace(workspace)
        if (prefix.isEmpty() || !id.startsWith(prefix)) {
            return id
        }
        return id.replaceFirst(prefix, "")
    }

    override fun addWsPrefixToId(localId: String, workspace: String): String {
        val prefix = getPrefixForIdInWorkspace(workspace)
        return if (localId.startsWith(prefix)) {
            localId
        } else {
            prefix + localId
        }
    }

    override fun convertToIdInWs(strId: String): IdInWs {
        return if (strId.contains(IdInWs.WS_DELIM)) {
            val wsSysId = strId.substringBefore(IdInWs.WS_DELIM)
            val idInWs = strId.substringAfter(IdInWs.WS_DELIM)
            val workspaceId = getWorkspaceIdBySystemId(wsSysId)
            if (workspaceId.isBlank()) {
                create("", strId)
            } else {
                create(workspaceId, idInWs)
            }
        } else {
            create("", strId)
        }
    }

    override fun convertToStrId(idInWs: IdInWs): String {
        if (idInWs.workspace.isEmpty() || idInWs.id.isEmpty()) {
            return idInWs.id
        }
        return addWsPrefixToId(idInWs.id, idInWs.workspace)
    }

    private fun getPrefixForIdInWorkspace(workspace: String): String {
        if (isWorkspaceWithGlobalArtifacts(workspace)) {
            return ""
        }
        val wsSysId = getWorkspaceSystemId(workspace)
        if (wsSysId.isBlank()) {
            return ""
        }
        return wsSysId + IdInWs.WS_DELIM
    }

    private fun isOwnPersonalWorkspace(user: String, workspace: String): Boolean {
        return workspace.startsWith(USER_WORKSPACE_PREFIX) && workspace.substring(USER_WORKSPACE_PREFIX.length) == user
    }

    private fun getWorkspacesByApi(user: String, membershipType: WsMembershipType): Set<String> {
        return AuthContext.runAsSystem {
            workspaceApi.getUserWorkspaces(user, membershipType)
        }
    }
}
