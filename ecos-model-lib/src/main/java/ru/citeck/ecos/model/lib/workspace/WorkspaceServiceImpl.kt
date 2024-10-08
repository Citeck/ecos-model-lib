package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.txn.lib.TxnContext

class WorkspaceServiceImpl(services: ModelServiceFactory) : WorkspaceService {

    companion object {
        private const val USER_WORKSPACES_CACHE_KEY = "user-workspaces-txn-cache-key"
    }

    private val workspaceApi = services.workspaceApi

    override fun getUserWorkspaces(user: String): Set<String> {
        if (getUserWorkspaceNotAllowed(user)) {
            return emptySet()
        }

        val userWorkspaces = TxnContext.getTxnOrNull()
            ?.getData(USER_WORKSPACES_CACHE_KEY) { HashMap<String, Set<String>>() }
            ?.computeIfAbsent(user) { getWorkspacesByApi(it) }
            ?: getWorkspacesByApi(user)

        val result = LinkedHashSet<String>(userWorkspaces)
        result.add("$USER_WORKSPACE_PREFIX$user")

        return result
    }

    private fun getUserWorkspaceNotAllowed(user: String): Boolean {
        if (AuthContext.isRunAsSystemOrAdmin()) {
            return false
        }

        val currentUser = AuthContext.getCurrentUser()
        return currentUser.isBlank() || currentUser != user
    }

    override fun isUserManagerOf(user: String, workspace: String): Boolean {
        return AuthContext.runAsSystem {
            workspaceApi.isUserManagerOf(user, workspace)
        }
    }

    private fun getWorkspacesByApi(user: String): Set<String> {
        return AuthContext.runAsSystem {
            workspaceApi.getUserWorkspaces(user)
        }
    }
}
