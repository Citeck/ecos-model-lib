package ru.citeck.ecos.model.lib.workspace

import com.github.benmanes.caffeine.cache.Caffeine
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.context.lib.auth.AuthRole
import ru.citeck.ecos.context.lib.auth.data.AuthData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.model.lib.workspace.IdInWs.Companion.create
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.records2.predicate.model.Predicate
import ru.citeck.ecos.records2.predicate.model.Predicates
import ru.citeck.ecos.txn.lib.TxnContext
import java.time.Duration

class WorkspaceServiceImpl(services: ModelServiceFactory) : WorkspaceService {

    companion object {
        private const val USER_WORKSPACES_CACHE_KEY = "user-workspaces-txn-cache-key"
        private const val WS_REF_PREFIX = "emodel/workspace@"
        private const val WS_SYSTEM_USERNAME_PREFIX = "ws_system_"
        private const val WS_PREFIX_MASK = "CURRENT_WS_ID${IdInWs.WS_DELIM}"
    }

    private val wsPrefixRegex = Regex("^.+?${IdInWs.WS_DELIM}")

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
            if (workspace.startsWith(USER_WORKSPACE_PREFIX) || isWorkspaceWithGlobalEntities(workspace)) {
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

    override fun isWorkspaceWithGlobalEntities(workspace: String?): Boolean {
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

    override fun getWorkspaceSystemId(workspace: String?): String {
        workspace ?: return ""
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

    override fun replaceWsPrefixFromIdToMask(id: String): String {
        return if (wsPrefixRegex.containsMatchIn(id)) {
            wsPrefixRegex.replaceFirst(id, WS_PREFIX_MASK)
        } else {
            id
        }
    }

    override fun replaceMaskFromIdToWsPrefix(id: String, workspace: String): String {
        if (!id.startsWith(WS_PREFIX_MASK)) {
            return id
        }

        val prefix = getPrefixForIdInWorkspace(workspace)
        return if (prefix.isEmpty()) {
            id
        } else {
            id.replaceFirst(WS_PREFIX_MASK, prefix)
        }
    }

    override fun convertToIdInWs(strId: String): IdInWs {
        return if (strId.contains(IdInWs.WS_DELIM)) {
            val wsSysId = strId.substringBefore(IdInWs.WS_DELIM)
            if (wsSysId.contains('$')) {
                // workspace system id should not contain '$' char
                return create("", strId)
            }
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

    override fun getArtifactsWritePermission(user: String, workspace: String?, typeId: String): Boolean {
        if (AuthContext.isRunAsSystemOrAdmin()) {
            return true
        }
        if (workspace == null || isWorkspaceWithGlobalEntities(workspace)) {
            return false
        }
        return isUserManagerOf(user, workspace)
    }

    override fun buildAvailableWorkspacesPredicate(auth: AuthData, queriedWorkspaces: List<String>): Predicate {
        val fixedWorkspaces = getAvailableWorkspacesToQuery(auth, queriedWorkspaces) ?: return Predicates.alwaysFalse()
        if (fixedWorkspaces.isEmpty()) {
            return Predicates.alwaysTrue()
        }
        return Predicates.inVals("workspace", fixedWorkspaces)
    }

    override fun getAvailableWorkspacesToQuery(auth: AuthData, queriedWorkspaces: List<String>): Set<String>? {
        val isSystem = AuthContext.isSystemAuth(auth)
        return if (queriedWorkspaces.isEmpty()) {
            if (isSystem) {
                return emptySet()
            } else {
                getUserOrWsSystemUserWorkspaces(auth) ?: return null
            }
        } else {
            var workspacesToQuery: Set<String> = queriedWorkspaces.mapTo(LinkedHashSet()) {
                if (isWorkspaceWithGlobalEntities(it)) "" else it
            }
            workspacesToQuery = expandWorkspaces(workspacesToQuery)
            if (!isSystem) {
                val currentUserWorkspaces = getUserOrWsSystemUserWorkspaces(auth) ?: return null
                workspacesToQuery = workspacesToQuery.filterTo(LinkedHashSet()) {
                    it == "" || currentUserWorkspaces.contains(it)
                }
                if (workspacesToQuery.isEmpty()) {
                    return null
                }
            }
            workspacesToQuery
        }
    }

    override fun getUserOrWsSystemUserWorkspaces(auth: AuthData): Set<String>? {
        return if (auth.getAuthorities().contains(ModelUtils.WORKSPACE_SYSTEM_ROLE)) {
            val user = auth.getUser()
            if (user.startsWith(WS_SYSTEM_USERNAME_PREFIX)) {
                val wsSysId = user.substring(WS_SYSTEM_USERNAME_PREFIX.length)
                val workspaceId = getWorkspaceIdBySystemId(wsSysId)
                val result = LinkedHashSet<String>(4)
                result.add(workspaceId)
                result.addAll(nestedWorkspacesCache[workspaceId])
                result.add("")
                return result
            } else {
                null
            }
        } else {
            val userWorkspaces = HashSet(getUserWorkspaces(auth.getUser()))
            userWorkspaces.add("")
            userWorkspaces
        }
    }

    override fun <T> runAsWsSystem(workspace: String, action: () -> T): T {
        val wsSysId = getWorkspaceSystemId(workspace)
        if (wsSysId.isBlank()) {
            error("Workspace doesn't have system identifier: '$workspace'")
        }
        return runAsWsSystemBySystemId(wsSysId, action)
    }

    override fun <T> runAsWsSystemBySystemId(wsSysId: String, action: () -> T): T {
        if (wsSysId.isBlank()) {
            throw IllegalArgumentException("Workspace system id is empty")
        }
        val userName = WS_SYSTEM_USERNAME_PREFIX + wsSysId
        return AuthContext.runAs(userName, listOf(ModelUtils.WORKSPACE_SYSTEM_ROLE, AuthRole.USER), action)
    }

    override fun isRunAsSystemOrWsSystem(workspace: String?): Boolean {
        return isSystemOrWsSystemAuth(AuthContext.getCurrentRunAsAuth(), workspace)
    }

    override fun isSystemOrWsSystemAuth(auth: AuthData, workspace: String?): Boolean {
        if (AuthContext.isSystemAuth(auth)) {
            return true
        }
        val user = auth.getUser()
        val authorities = auth.getAuthorities()
        if (!authorities.contains(ModelUtils.WORKSPACE_SYSTEM_ROLE) || !user.startsWith(WS_SYSTEM_USERNAME_PREFIX)) {
            return false
        }
        val wsSysId = getWorkspaceSystemId(workspace)
        return user.endsWith(wsSysId) && (WS_SYSTEM_USERNAME_PREFIX.length + wsSysId.length == user.length)
    }

    override fun isSystemOrWsSystemOrAdminAuth(auth: AuthData, workspace: String?): Boolean {
        return isSystemOrWsSystemAuth(auth, workspace) || AuthContext.isAdminAuth(auth)
    }

    override fun getUpdatedWsInMutation(currentWs: String, ctxWorkspace: String?): String {
        if (currentWs.isNotBlank() ||
            ctxWorkspace.isNullOrBlank() ||
            isWorkspaceWithGlobalEntities(ctxWorkspace)
        ) {
            return currentWs
        }
        return ctxWorkspace.replace(WS_REF_PREFIX, "")
    }

    private fun getPrefixForIdInWorkspace(workspace: String): String {
        if (isWorkspaceWithGlobalEntities(workspace)) {
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
