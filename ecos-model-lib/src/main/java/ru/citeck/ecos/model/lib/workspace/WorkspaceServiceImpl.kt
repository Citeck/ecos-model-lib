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

        val userWorkspaces = TxnContext.getTxnOrNull()
            ?.getData(USER_WORKSPACES_CACHE_KEY) { HashMap<String, Set<String>>() }
            ?.computeIfAbsent(user) { getWorkspacesByApi(it) }
            ?: getWorkspacesByApi(user)

        val result = LinkedHashSet<String>(userWorkspaces)
        result.add("user$$user")

        return result
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
