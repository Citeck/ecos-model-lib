package ru.citeck.ecos.model.lib.role.service

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.computed.ComputeDmnDecisionWebApi
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.txn.lib.TxnContext
import ru.citeck.ecos.webapp.api.entity.EntityRef

class ComputedRoleProcessor(
    private val services: ModelServiceFactory,
    private val computeDmnDecisionWebApi: ComputeDmnDecisionWebApi = ComputeDmnDecisionWebApi(services),
    private val recordsService: RecordsService = services.records.recordsService
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    private val dmnComputeTxnCacheKey = Any()

    fun computeRoleAssigneesFromDmn(decisionRef: EntityRef, record: Any?): List<String> {
        if (decisionRef.isEmpty()) {
            return emptyList()
        }

        val model = recordsService.getAtt(decisionRef, "definition.model")
            .asMap(String::class.java, String::class.java)

        val filledModel = mutableMapOf<String, Any>()

        AuthContext.runAsSystem {
            recordsService.getAtts(record, model).forEach { key, attr ->
                filledModel[key] = attr
            }
        }

        val cache = TxnContext.getReadOnlyCache<DmnCacheKey, List<String>>(dmnComputeTxnCacheKey)
        // Snapshot inputs so mutable record values cannot change an existing cache key.
        val key = DmnCacheKey(decisionRef, record.toString(), Json.mapper.toStringNotNull(filledModel))
        cache[key]?.let { assignees ->
            log.trace {
                "Assignees from DMN decision cache: \n$key, \nassignees: $assignees"
            }
            return assignees
        }

        val assigneesFromDmn = computeDmnDecisionWebApi.compute(EntityRef.valueOf(decisionRef), filledModel)
            .result
            .values
            .flatten()
            .map { dmnEvalEntryValue ->
                dmnEvalEntryValue.toString().split(",").map { it.trim() }
            }
            .flatten()

        log.trace {
            "Assignees from DMN decision: \n$decisionRef, \nmodel: $model, " +
                "\nfilledModel: $filledModel \nassignees: $assigneesFromDmn"
        }

        cache[key] = assigneesFromDmn

        return assigneesFromDmn
    }

    private data class DmnCacheKey(
        val decisionRef: EntityRef,
        val record: String,
        val inputs: String
    )
}
