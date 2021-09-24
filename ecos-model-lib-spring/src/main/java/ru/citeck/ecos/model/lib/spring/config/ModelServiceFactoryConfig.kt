package ru.citeck.ecos.model.lib.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.RecordPermsService
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.model.lib.role.service.auth.AuthorityComponent
import ru.citeck.ecos.model.lib.status.service.StatusService
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.spring.config.RecordsServiceFactoryConfiguration

@Configuration
open class ModelServiceFactoryConfig : ModelServiceFactory() {

    private var custonTypesRepo: TypesRepo? = null
    private var customPermsRepo: PermissionsRepo? = null
    private var authorityComponentBean: AuthorityComponent? = null

    @Bean
    override fun createTypeRefService(): TypeRefService {
        val service = super.createTypeRefService()
        val recordsServices = records
        if (recordsServices is RecordsServiceFactoryConfiguration) {
            recordsServices.setCustomRecordTypeService(service)
        }
        return service
    }

    @Bean
    override fun createRoleService(): RoleService {
        return super.createRoleService()
    }

    @Bean
    override fun createStatusService(): StatusService {
        return super.createStatusService()
    }

    @Bean
    override fun createRecordPermsService(): RecordPermsService {
        return super.createRecordPermsService()
    }

    override fun createTypesRepo(): TypesRepo {
        return object : TypesRepo {
            override fun getModel(typeRef: RecordRef): TypeModelDef {
                return custonTypesRepo?.getModel(typeRef) ?: TypeModelDef.EMPTY
            }
            override fun getParent(typeRef: RecordRef): RecordRef {
                return custonTypesRepo?.getParent(typeRef) ?: RecordRef.EMPTY
            }
            override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                return custonTypesRepo?.getChildren(typeRef) ?: emptyList()
            }
        }
    }

    override fun createPermissionsRepo(): PermissionsRepo {
        return object : PermissionsRepo {
            override fun getPermissionsForType(typeRef: RecordRef): TypePermsDef? {
                return customPermsRepo?.getPermissionsForType(typeRef)
            }
        }
    }

    override fun createAuthorityComponent(): AuthorityComponent {
        return authorityComponentBean ?: super.createAuthorityComponent()
    }

    @Autowired(required = false)
    fun setTypesRepo(repo: TypesRepo) {
        this.custonTypesRepo = repo
    }

    @Autowired(required = false)
    fun setPermsRepo(repo: PermissionsRepo) {
        this.customPermsRepo = repo
    }

    @Autowired(required = false)
    fun setAuthorityComponent(authorityComponent: AuthorityComponent) {
        this.authorityComponentBean = authorityComponent
    }

    @Autowired
    override fun setRecordsServices(services: RecordsServiceFactory) {
        super.setRecordsServices(services)
    }
}
