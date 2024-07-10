package ru.citeck.ecos.model.lib.delegation.service

import mu.KotlinLogging
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation
import ru.citeck.ecos.model.lib.delegation.dto.PermissionDelegateData
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName

private const val PERMISSION_DELEGATE_TYPE = "type@permission-delegate"
private const val PERMISSION_DELEGATES_ATT = "permission-delegated:permissionDelegates"
private const val PERMISSION_DELEGATE_TYPE_ATT = "permissionType"
private const val PERMISSION_DELEGATE_FROM_ATT = "from"
private const val PERMISSION_DELEGATE_TO_ATT = "to"

open class DefaultDelegationService(services: ModelServiceFactory) : DelegationService {

    private val recordsService = services.records.recordsServiceV1
    private val delegationApi = services.delegationApi

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation> {
        return delegationApi.getActiveAuthDelegations(user, types)
    }

    override fun getPermissionDelegates(record: Any, permission: PermissionType): List<PermissionDelegateData> {
        return AuthContext.runAsSystem {
            val currentDelegates = recordsService.getAtts(record, PermissionDelegates::class.java)
            currentDelegates.delegates
                .filter { it.isNotEmpty() || it.permissionType == permission }
        }
    }

    override fun delegatePermission(record: Any, permission: PermissionType, from: String, to: String) {
        log.debug { "Delegate permission $permission from $from to $to" }

        AuthContext.runAsSystem {
            delegateImpl(record, permission, from, to)
        }
    }

    private fun delegateImpl(record: Any, permission: PermissionType, from: String, to: String) {
        if (delegationAlreadyExists(record, permission, from, to)) {
            log.debug { "Delegation already exists" }
            return
        }

        recordsService.create(
            PERMISSION_DELEGATE_TYPE,
            mapOf(
                RecordConstants.ATT_PARENT to record,
                RecordConstants.ATT_PARENT_ATT to PERMISSION_DELEGATES_ATT,
                PERMISSION_DELEGATE_TYPE_ATT to permission.name,
                PERMISSION_DELEGATE_FROM_ATT to from,
                PERMISSION_DELEGATE_TO_ATT to to
            )
        )
    }

    private fun delegationAlreadyExists(
        record: Any,
        permission: PermissionType,
        from: String,
        to: String
    ): Boolean {
        val currentDelegates = recordsService.getAtts(record, PermissionDelegates::class.java)
        return currentDelegates.delegates.any {
            it.permissionType == permission && it.from == from && it.to.contains(to)
        }
    }

    private data class PermissionDelegates(
        @AttName(PERMISSION_DELEGATES_ATT)
        val delegates: List<PermissionDelegateData> = emptyList()
    )
}
