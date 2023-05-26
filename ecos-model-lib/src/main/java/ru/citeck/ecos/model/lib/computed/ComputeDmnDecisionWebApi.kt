package ru.citeck.ecos.model.lib.computed

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.entity.EntityRef
import ru.citeck.ecos.webapp.api.web.client.EcosWebClientApi

class ComputeDmnDecisionWebApi(
    private val services: ModelServiceFactory,
    private val webClient: EcosWebClientApi? = services.getEcosWebAppApi()?.getWebClientApi()
) {

    companion object {
        const val PATH = "/dmn/evaluate-decision/collect-map-entries"
    }

    fun compute(
        decisionRef: EntityRef,
        variables: Map<String, Any>
    ): ComputeDmnDecisionResponseDto {
        if (webClient == null) {
            error("EcosWebClientApi is null")
        }

        val apiVersion = webClient.getApiVersion(AppName.EPROC, PATH, 0)
        if (apiVersion < 0) {
            error("DMN evaluate-decision API is not available")
        }

        return webClient.newRequest()
            .targetApp(AppName.EPROC)
            .path(PATH)
            .body {
                it.writeDto(ComputeDmnDecisionRequestDto(decisionRef, variables))
            }
            .executeSync {
                it.getBodyReader().readDto(ComputeDmnDecisionResponseDto::class.java)
            }
    }
}

data class ComputeDmnDecisionRequestDto(
    val decisionRef: EntityRef = EntityRef.EMPTY,
    val variables: Map<String, Any> = emptyMap()
)

data class ComputeDmnDecisionResponseDto(
    val result: Map<String, List<Any>> = emptyMap()
)
