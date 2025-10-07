package ru.citeck.ecos.model.lib.workspace.api

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.web.client.EcosWebClientApi

class WorkspaceWebApi(
    private val webClient: EcosWebClientApi?
) : WorkspaceApi {

    companion object {
        const val GET_NESTED_WORKSPACES_PATH = "/workspace/nested-workspaces/get"
        const val GET_USER_WORKSPACES_PATH = "/workspace/user-workspaces/get"
        const val IS_USER_MANAGER_OF_PATH = "/workspace/is-user-manager-of"
        const val GET_IDS_MAPPING_PATH = "/workspace/ids-mapping/get"

        private val log = KotlinLogging.logger {}
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

    override fun mapIdentifiers(identifiers: List<String>, mappingType: WorkspaceApi.IdMappingType): List<String> {

        if (mappingType == WorkspaceApi.IdMappingType.NO_MAPPING) {
            return identifiers
        }

        val webClient = webClient ?: error("WebClient is null")

        var apiVersion = webClient.getApiVersion(AppName.EMODEL, GET_IDS_MAPPING_PATH, 0)
        if (apiVersion == EcosWebClientApi.AV_APP_NOT_AVAILABLE) {
            apiVersion = waitUntilEmodelApiIsAvailable(webClient, GET_IDS_MAPPING_PATH)
        }

        if (apiVersion < 0) {
            error(
                "Remote API \"/${AppName.EMODEL}/${GET_IDS_MAPPING_PATH}/\" is not implemented. " +
                    "Please, check that version of ${AppName.EMODEL} is greater or equal 2.33.0"
            )
        }

        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(GET_IDS_MAPPING_PATH)
            .body {
                it.writeDto(GetIdsMappingReq(identifiers.toList(), mappingType.id))
            }
            .executeSync {
                it.getBodyReader().readDto(GetIdsMappingResp::class.java)
            }.ids
    }

    @Suppress("SameParameterValue")
    private fun waitUntilEmodelApiIsAvailable(webClient: EcosWebClientApi, apiPath: String): Int {
        val sleepTime = 5_000L
        var logIteration = -1L
        val logPeriod = 30_000L
        val waitingStartedAt = System.currentTimeMillis()
        var apiVersion: Int
        do {
            val elapsedTime = System.currentTimeMillis() - waitingStartedAt
            val nextLogIteration = elapsedTime / logPeriod
            if (nextLogIteration != logIteration) {
                logIteration = nextLogIteration
                val logMsg = "Model service is not available. Waiting... Elapsed time: ${elapsedTime}ms"
                if (elapsedTime < 30_000) {
                    log.debug { logMsg }
                } else if (elapsedTime < 120_000) {
                    log.warn { logMsg }
                } else {
                    log.error { logMsg }
                }
            }
            Thread.sleep(sleepTime)
            apiVersion = webClient.getApiVersion(AppName.EMODEL, apiPath, 0)
        } while (apiVersion == EcosWebClientApi.AV_APP_NOT_AVAILABLE)

        return apiVersion
    }

    data class GetIdsMappingReq(
        val ids: List<String>,
        /**
         * @see WorkspaceApi.IdMappingType.id
         */
        val mappingType: Int
    )

    data class GetIdsMappingResp(
        val ids: List<String>
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
