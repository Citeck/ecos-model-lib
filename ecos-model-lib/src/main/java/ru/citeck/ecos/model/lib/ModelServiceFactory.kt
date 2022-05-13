package ru.citeck.ecos.model.lib

import ru.citeck.ecos.model.lib.api.DefaultModelAppApi
import ru.citeck.ecos.model.lib.api.EcosModelAppApi
import ru.citeck.ecos.model.lib.attributes.computed.ComputedAttsService
import ru.citeck.ecos.model.lib.num.repo.DefaultNumTemplatesRepo
import ru.citeck.ecos.model.lib.num.repo.NumTemplatesRepo
import ru.citeck.ecos.model.lib.num.service.EcosNumService
import ru.citeck.ecos.model.lib.permissions.repo.DefaultPermissionsRepo
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.PermsEvaluator
import ru.citeck.ecos.model.lib.permissions.service.RecordPermsService
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.model.lib.role.service.auth.AuthorityComponent
import ru.citeck.ecos.model.lib.role.service.auth.DefaultAuthorityComponent
import ru.citeck.ecos.model.lib.status.service.StatusService
import ru.citeck.ecos.model.lib.type.repo.DefaultTypesRepo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.RecordTypeServiceImpl
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.context.EcosWebAppContext
import ru.citeck.ecos.webapp.api.properties.EcosWebAppProperties

open class ModelServiceFactory {

    val permsEvaluator: PermsEvaluator by lazy { createPermsEvaluator() }
    val typeRefService: TypeRefService by lazy { createTypeRefService() }
    val permissionsRepo: PermissionsRepo by lazy { createPermissionsRepo() }
    val recordPermsService: RecordPermsService by lazy { createRecordPermsService() }
    val roleService: RoleService by lazy { createRoleService() }
    val statusService: StatusService by lazy { createStatusService() }
    val authorityComponent: AuthorityComponent by lazy { createAuthorityComponent() }
    val ecosModelAppApi: EcosModelAppApi by lazy { createEcosModelAppApi() }
    val ecosNumService: EcosNumService by lazy { createEcosNumService() }
    val computedAttsService: ComputedAttsService by lazy { createComputedAttsToStoreService() }

    val typesRepo: TypesRepo by lazy { createTypesRepo() }
    val numTemplatesRepo: NumTemplatesRepo by lazy { createNumTemplatesRepo() }

    lateinit var records: RecordsServiceFactory
        private set

    val webappProps by lazy {
        getEcosWebAppContext()?.getProperties() ?: EcosWebAppProperties("", "")
    }

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

    protected open fun createNumTemplatesRepo(): NumTemplatesRepo {
        return DefaultNumTemplatesRepo()
    }

    protected open fun createRecordPermsService(): RecordPermsService {
        return RecordPermsService(this)
    }

    protected open fun createAuthorityComponent(): AuthorityComponent {
        return DefaultAuthorityComponent()
    }

    protected open fun createEcosModelAppApi(): EcosModelAppApi {
        return DefaultModelAppApi()
    }

    protected open fun createEcosNumService(): EcosNumService {
        return EcosNumService(this)
    }

    protected open fun createComputedAttsToStoreService(): ComputedAttsService {
        return ComputedAttsService(this)
    }

    open fun setRecordsServices(services: RecordsServiceFactory) {
        this.records = services
        services.setRecordTypeService(RecordTypeServiceImpl(this))
    }

    open fun getEcosWebAppContext(): EcosWebAppContext? {
        return null
    }
}
