package ru.citeck.ecos.model.lib.permissions.evaluator

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.service.PermsEvaluator
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.dao.atts.RecordAttsDao

abstract class PermsEvaluatorTestBase {

    protected val evaluator: PermsEvaluator

    init {
        val services = ModelServiceFactory()
        services.setRecordsServices(RecordsServiceFactory())
        services.records.recordsServiceV1.register(
            object : RecordAttsDao {
                override fun getId() = "test"
                override fun getRecordAtts(recordId: String) = TestDto(recordId)
            }
        )
        evaluator = services.permsEvaluator
    }

    fun getPerms(status: String,
                 roles: Collection<String>,
                 statuses: Collection<String>,
                 permsDef: PermissionsDef): RolesPermissions {

        return evaluator.getPermissions(RecordRef.create("test", status), roles, statuses, permsDef)
    }

    class TestDto(val _status: String)
}
