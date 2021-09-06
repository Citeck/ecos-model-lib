package ru.citeck.ecos.model.lib.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.service.auth.AuthorityComponent
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory

@Configuration
open class ModelServiceFactoryConfig : ModelServiceFactory() {

    private var repo: TypesRepo? = null
    private var authorityComponentBean: AuthorityComponent? = null

    @Bean
    override fun createTypeRefService(): TypeRefService {
        return super.createTypeRefService()
    }

    override fun createTypesRepo(): TypesRepo {
        return object : TypesRepo {
            override fun getModel(typeRef: RecordRef): TypeModelDef {
                return repo?.getModel(typeRef) ?: TypeModelDef.EMPTY
            }
            override fun getParent(typeRef: RecordRef): RecordRef {
                return repo?.getParent(typeRef) ?: RecordRef.EMPTY
            }
            override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                return repo?.getChildren(typeRef) ?: emptyList()
            }
        }
    }

    override fun createAuthorityComponent(): AuthorityComponent {
        return authorityComponentBean ?: super.createAuthorityComponent()
    }

    @Autowired(required = false)
    fun setTypesRepo(repo: TypesRepo) {
        this.repo = repo
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
