package ru.citeck.ecos.model.lib.permissions.evaluator

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionRule
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.service.PermsEvaluator
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao
import ru.citeck.ecos.records3.record.dao.atts.RecordAttsDao
import ru.citeck.ecos.records3.record.mixin.AttMixin
import ru.citeck.ecos.webapp.api.entity.EntityRef

abstract class PermsEvaluatorTestBase {

    companion object {
        private val EMPTY_TEST_REF = EntityRef.create("test", "")
    }

    protected val evaluator: PermsEvaluator

    private var records: MutableMap<String, Any> = hashMapOf()
    private var currentStatus: String = "draft"

    private var roles = listOf("initiator", "approver")
    private var statuses = listOf("draft", "approve")

    init {
        val services = ModelServiceFactory()
        services.setRecordsServices(RecordsServiceFactory())

        val dao = object : AbstractRecordsDao(), RecordAttsDao {
            override fun getId() = "test"
            override fun getRecordAtts(recordId: String): Any? {
                return if (recordId.isBlank()) {
                    TestDto()
                } else {
                    records[recordId]
                }
            }
        }
        dao.addAttributesMixin(object : AttMixin {
            override fun getAtt(path: String, value: AttValueCtx) = currentStatus
            override fun getProvidedAtts() = listOf("_status")
        })
        services.records.recordsServiceV1.register(dao)

        evaluator = services.permsEvaluator
    }

    fun addRecord(id: String, record: Any) {
        this.records[id] = record
    }

    fun setStatusForRecord(status: String) {
        this.currentStatus = status
    }

    fun setRoles(roles: List<String>) {
        this.roles = roles
    }

    fun setStatuses(statuses: List<String>) {
        this.statuses = statuses
    }

    fun getNoneMatrix(statuses: List<String>, roles: List<String>): Map<String, Map<String, PermissionLevel>> {
        return roles.associateWith {
            statuses.associateWith { PermissionLevel.NONE }
        }
    }

    fun getPerms(recordId: String, permsDef: PermissionsDef): RolesPermissions {

        return evaluator.getPermissions(EntityRef.create("test", recordId), roles, statuses, permsDef)
    }

    fun getPerms(permsDef: PermissionsDef): RolesPermissions {

        return evaluator.getPermissions(EMPTY_TEST_REF, roles, statuses, permsDef)
    }

    fun createPermsDefWithRules(rules: List<PermissionRule>): PermissionsDef {
        return PermissionsDef.create {
            withMatrix(getNoneMatrix(statuses, roles))
            withRules(rules)
        }
    }

    private class TestDto
}
