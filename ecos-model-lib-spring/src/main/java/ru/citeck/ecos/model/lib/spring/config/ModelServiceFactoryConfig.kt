package ru.citeck.ecos.model.lib.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.citeck.ecos.commands.CommandsServiceFactory
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.api.EcosModelAppApi
import ru.citeck.ecos.model.lib.api.commands.CommandsModelAppApi
import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.model.lib.num.repo.NumTemplatesRepo
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.RecordPermsService
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.model.lib.role.service.auth.AuthorityComponent
import ru.citeck.ecos.model.lib.status.service.StatusService
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory

@Configuration
open class ModelServiceFactoryConfig : ModelServiceFactory() {

    private var customTypesRepo: TypesRepo? = null
    private var customPermsRepo: PermissionsRepo? = null
    private var customNumTemplatesRepo: NumTemplatesRepo? = null

    private var authorityComponentBean: AuthorityComponent? = null

    private lateinit var commandsServices: CommandsServiceFactory

    @Bean
    override fun createTypeRefService(): TypeRefService {
        return super.createTypeRefService()
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

    override fun createNumTemplatesRepo(): NumTemplatesRepo {
        return object : NumTemplatesRepo {
            override fun getNumTemplate(templateRef: RecordRef): NumTemplateDef? {
                return customNumTemplatesRepo?.getNumTemplate(templateRef)
            }
        }
    }

    override fun createTypesRepo(): TypesRepo {
        return object : TypesRepo {
            override fun getTypeInfo(typeRef: RecordRef): TypeInfo? {
                return customTypesRepo?.getTypeInfo(typeRef)
            }
            override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                return customTypesRepo?.getChildren(typeRef) ?: emptyList()
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

    override fun createEcosModelAppApi(): EcosModelAppApi {
        return CommandsModelAppApi(commandsServices)
    }

    @Autowired(required = false)
    fun setTypesRepo(repo: TypesRepo) {
        this.customTypesRepo = repo
    }

    @Autowired(required = false)
    fun setPermsRepo(repo: PermissionsRepo) {
        this.customPermsRepo = repo
    }

    @Autowired(required = false)
    fun setAuthorityComponent(authorityComponent: AuthorityComponent) {
        this.authorityComponentBean = authorityComponent
    }

    @Autowired(required = false)
    fun setNumTemplatesRepo(repo: NumTemplatesRepo) {
        this.customNumTemplatesRepo = repo
    }

    @Autowired
    override fun setRecordsServices(services: RecordsServiceFactory) {
        super.setRecordsServices(services)
    }

    @Autowired
    fun setCommandsServices(services: CommandsServiceFactory) {
        this.commandsServices = services
    }
}
