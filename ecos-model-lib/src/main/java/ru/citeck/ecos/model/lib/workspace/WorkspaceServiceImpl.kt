package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.txn.lib.TxnContext

class WorkspaceServiceImpl(services: ModelServiceFactory) : WorkspaceService {

    private val workspaceApi = services.workspaceApi

    override fun getUserWorkspaces(user: String, authorities: Collection<String>): Set<String> {

        val key = user to authorities

        val userWorkspaces = TxnContext.getTxnOrNull()?.getData(key) {
            getWorkspacesByApi(it)
        } ?: getWorkspacesByApi(key)

        val result = LinkedHashSet<String>(userWorkspaces)
        result.add("user$$user")

        return result
    }

    private fun getWorkspacesByApi(userWithAuthorities: Pair<String, Collection<String>>): Set<String> {
        return AuthContext.runAsSystem {
            workspaceApi.getUserWorkspaces(userWithAuthorities.first, userWithAuthorities.second.toList())
        }
    }
}
