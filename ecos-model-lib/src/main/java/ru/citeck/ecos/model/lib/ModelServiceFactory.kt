package ru.citeck.ecos.model.lib

import ru.citeck.ecos.model.lib.permissions.repo.DefaultPermissionsRepo
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.PermsEvaluator
import ru.citeck.ecos.model.lib.permissions.service.RecordPermsService
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.model.lib.status.service.StatusService
import ru.citeck.ecos.model.lib.type.repo.DefaultTypesRepo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records3.RecordsServiceFactory

open class ModelServiceFactory {

    val permsEvaluator: PermsEvaluator by lazy { createPermsEvaluator() }
    val typeRefService: TypeRefService by lazy { createTypeRefService() }
    val permissionsRepo: PermissionsRepo by lazy { createPermissionsRepo() }
    val recordPermsService: RecordPermsService by lazy { createRecordPermsService() }
    val roleService: RoleService by lazy { createRoleService() }
    val statusService: StatusService by lazy { createStatusService() }

    val typesRepo: TypesRepo by lazy { createTypesRepo() }

    lateinit var records: RecordsServiceFactory
        private set

    protected open fun createRoleService(): RoleService {
        return RoleService(this)
    }

    protected open fun createStatusService(): StatusService {
        return StatusService(this)
    }

    protected open fun createPermsEvaluator(): PermsEvaluator {
        return PermsEvaluator(this)
    }

    protected open fun createTypesRepo(): TypesRepo {
        return DefaultTypesRepo()
    }

    protected open fun createTypeRefService(): TypeRefService {
        return TypeRefService(this)
    }

    protected open fun createPermissionsRepo(): PermissionsRepo {
        return DefaultPermissionsRepo()
    }

    protected open fun createRecordPermsService(): RecordPermsService {
        return RecordPermsService(this)
    }

    open fun setRecordsServices(services: RecordsServiceFactory) {
        this.records = services
        services.setRecordTypeService(typeRefService)
    }
}
