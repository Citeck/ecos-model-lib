package ru.citeck.ecos.model.lib.workspace.api

import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.web.client.EcosWebClientApi

class WorkspaceWebApi(
    private val webClient: EcosWebClientApi?
) : WorkspaceApi {

    companion object {
        const val GET_USER_WORKSPACES_PATH = "/workspace/user-workspaces/get"
    }

    override fun getUserWorkspaces(user: String): Set<String> {

        val webClient = webClient ?: return emptySet()

        val apiVersion = webClient.getApiVersion(AppName.EMODEL, GET_USER_WORKSPACES_PATH, 0)
        if (apiVersion < 0) {
            return emptySet()
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(GET_USER_WORKSPACES_PATH)
            .body {
                it.writeDto(GetUserWorkspacesReq(user))
            }
            .executeSync {
                it.getBodyReader().readDto(GetUserWorkspacesResp::class.java)
            }.workspaces
    }

    data class GetUserWorkspacesReq(
        val user: String
    )

    data class GetUserWorkspacesResp(
        val workspaces: Set<String>
    )
}
