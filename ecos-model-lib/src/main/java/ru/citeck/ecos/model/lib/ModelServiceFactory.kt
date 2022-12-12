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
import ru.citeck.ecos.model.lib.status.service.StatusService
import ru.citeck.ecos.model.lib.type.repo.DefaultTypesRepo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.RecordTypeComponentImpl
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.EcosWebAppApi
import ru.citeck.ecos.webapp.api.properties.EcosWebAppProps
import java.util.concurrent.atomic.AtomicBoolean

open class ModelServiceFactory {

    val permsEvaluator: PermsEvaluator by lazySingleton { createPermsEvaluator() }
    val typeRefService: TypeRefService by lazySingleton { createTypeRefService() }
    val permissionsRepo: PermissionsRepo by lazySingleton { createPermissionsRepo() }
    val recordPermsService: RecordPermsService by lazySingleton { createRecordPermsService() }
    val roleService: RoleService by lazySingleton { createRoleService() }
    val statusService: StatusService by lazySingleton { createStatusService() }
    val ecosModelAppApi: EcosModelAppApi by lazySingleton { createEcosModelAppApi() }
    val ecosNumService: EcosNumService by lazySingleton { createEcosNumService() }
    val computedAttsService: ComputedAttsService by lazySingleton { createComputedAttsToStoreService() }

    val typesRepo: TypesRepo by lazySingleton { createTypesRepo() }
    val numTemplatesRepo: NumTemplatesRepo by lazySingleton { createNumTemplatesRepo() }

    lateinit var records: RecordsServiceFactory
        private set

    val webappProps by lazy {
        getEcosWebAppApi()?.getProperties() ?: EcosWebAppProps("", "")
    }

    protected open fun createRoleService(): RoleService {
        return RoleService()
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
        services.setRecordTypeComponent(RecordTypeComponentImpl(this))
    }

    open fun getEcosWebAppApi(): EcosWebAppApi? {
        return null
    }

    private fun <T> lazySingleton(initializer: () -> T): Lazy<T> {
        val initializationInProgress = AtomicBoolean()
        var createdValue: T? = null
        return lazy {
            if (initializationInProgress.compareAndSet(false, true)) {
                val value = initializer()
                createdValue = value
                if (value is ModelServiceFactoryAware) {
                    value.setModelServiceFactory(this)
                }
                value
            } else {
                createdValue ?: error("Cyclic reference")
            }
        }
    }
}
