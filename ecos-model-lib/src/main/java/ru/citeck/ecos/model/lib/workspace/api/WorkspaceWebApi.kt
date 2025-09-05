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
        const val GET_WORKSPACE_SYSTEM_ID_PATH = "/workspace/system-id/get"
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

    override fun getWorkspaceSysId(workspaces: List<String>): List<String> {

        val webClient = webClient ?: error("WebClient is null")

        val apiVersion = webClient.getApiVersion(AppName.EMODEL, GET_WORKSPACE_SYSTEM_ID_PATH, 0)
        if (apiVersion < 0) {
            error(
                "Remote API \"/${AppName.EMODEL}/${GET_WORKSPACE_SYSTEM_ID_PATH}/\" is not implemented. " +
                    "Please, check that version of ${AppName.EMODEL} is greater or equal 2.33.0"
            )
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(GET_WORKSPACE_SYSTEM_ID_PATH)
            .body {
                it.writeDto(GetWorkspaceSystemIdReq(workspaces.toList()))
            }
            .executeSync {
                it.getBodyReader().readDto(GetWorkspaceSystemIdResp::class.java)
            }.systemIds
    }

    data class GetWorkspaceSystemIdReq(
        val workspaces: List<String>
    )

    data class GetWorkspaceSystemIdResp(
        val systemIds: List<String>
    )

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
