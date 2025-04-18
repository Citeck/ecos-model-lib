package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.txn.lib.TxnContext

class WorkspaceServiceImpl(services: ModelServiceFactory) : WorkspaceService {

    companion object {
        private const val USER_WORKSPACES_CACHE_KEY = "user-workspaces-txn-cache-key"
    }

    private val workspaceApi = services.workspaceApi

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

    private fun isOwnPersonalWorkspace(user: String, workspace: String): Boolean {
        return workspace.startsWith(USER_WORKSPACE_PREFIX) && workspace.substring(USER_WORKSPACE_PREFIX.length) == user
    }

    private fun getWorkspacesByApi(user: String, membershipType: WsMembershipType): Set<String> {
        return AuthContext.runAsSystem {
            workspaceApi.getUserWorkspaces(user, membershipType)
        }
    }
}
