package ru.citeck.ecos.model.lib.delegation.api

import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.web.client.EcosWebClientApi

class DelegationWebApi(private val webClient: EcosWebClientApi?) : DelegationApi {

    companion object {
        const val AUTH_DELEGATIONS_GET_PATH = "/delegation/auth-delegations/get"
    }

    override fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation> {

        webClient ?: return emptyList()
        val apiVersion = webClient.getApiVersion(AppName.EMODEL, AUTH_DELEGATIONS_GET_PATH, 0)
        if (apiVersion < 0) {
            return emptyList()
        }
        return webClient.newRequest()
            .targetApp(AppName.EMODEL)
            .path(AUTH_DELEGATIONS_GET_PATH)
            .body {
                it.writeDto(AuthDelegationsGetReq(user, types.toSet()))
            }
            .executeSync {
                it.getBodyReader().readDto(AuthDelegationsGetResp::class.java)
            }.delegations
    }

    data class AuthDelegationsGetReq(
        val user: String,
        val types: Set<String>
    )

    data class AuthDelegationsGetResp(
        val delegations: List<AuthDelegation>
    )
}
