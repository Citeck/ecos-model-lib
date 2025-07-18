package ru.citeck.ecos.model.lib.workspace.api

import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.web.client.EcosWebClientApi

class WorkspaceWebApi(
    private val webClient: EcosWebClientApi?
) : WorkspaceApi {

    companion object {
        const val GET_NESTED_WORKSPACES_PATH = "/workspace/nested-workspaces/get"
        const val GET_USER_WORKSPACES_PATH = "/workspace/user-workspaces/get"
        const val IS_USER_MANAGER_OF_PATH = "/workspace/is-user-manager-of"
    }

    override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> {

        val webClient = webClient ?: return workspaces.map { emptySet() }

        val apiVersion = webClient.getApiVersion(AppName.EMODEL, GET_NESTED_WORKSPACES_PATH, 0)
        if (apiVersion < 0) {
            return workspaces.map { emptySet() }
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(GET_NESTED_WORKSPACES_PATH)
            .body {
                it.writeDto(GetNestedWorkspacesReq(workspaces))
            }
            .executeSync {
                it.getBodyReader().readDto(GetNestedWorkspacesResp::class.java)
            }.workspaces
    }

    override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> {

        val webClient = webClient ?: return emptySet()

        val apiVersion = webClient.getApiVersion(AppName.EMODEL, GET_USER_WORKSPACES_PATH, 0)
        if (apiVersion < 0) {
            return emptySet()
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(GET_USER_WORKSPACES_PATH)
            .body {
                it.writeDto(GetUserWorkspacesReq(user, membershipType))
            }
            .executeSync {
                it.getBodyReader().readDto(GetUserWorkspacesResp::class.java)
            }.workspaces
    }

    override fun isUserManagerOf(user: String, workspace: String): Boolean {

        val webClient = webClient ?: return false

        val apiVersion = webClient.getApiVersion(AppName.EMODEL, IS_USER_MANAGER_OF_PATH, 0)
        if (apiVersion < 0) {
            return false
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(IS_USER_MANAGER_OF_PATH)
            .body {
                it.writeDto(IsUserManagerOfReq(user, workspace))
            }
            .executeSync {
                it.getBodyReader().readDto(IsUserManagerOfResp::class.java)
            }.result
    }

    data class GetNestedWorkspacesReq(
        val workspaces: Collection<String>
    )

    data class GetNestedWorkspacesResp(
        /**
         * The list has the same size as in the request.
         * Each index matches the workspace from the request
         * and contains its nested workspaces.
         */
        val workspaces: List<Set<String>>
    )

    data class IsUserManagerOfReq(
        val user: String,
        val workspace: String
    )

    data class IsUserManagerOfResp(
        val result: Boolean
    )

    data class GetUserWorkspacesReq(
        val user: String,
        val membershipType: WsMembershipType = WsMembershipType.ALL
    )

    data class GetUserWorkspacesResp(
        val workspaces: Set<String>
    )
}
