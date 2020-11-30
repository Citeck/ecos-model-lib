package ru.citeck.ecos.model.lib.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeDefService
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory

@Configuration
open class ModelServiceFactoryConfig : ModelServiceFactory() {

    private var repo: TypesRepo? = null

    @Bean
    override fun createTypeDefService(): TypeDefService {
        return super.createTypeDefService()
    }

    override fun createTypesRepo(): TypesRepo {
        return object : TypesRepo {
            override fun getTypeDef(typeRef: RecordRef): TypeDef? {
                return repo?.getTypeDef(typeRef)
            }
            override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                return repo?.getChildren(typeRef) ?: emptyList()
            }
        }
    }

    @Autowired(required = false)
    fun setTypesRepo(repo: TypesRepo) {
        this.repo = repo
    }

    @Autowired
    override fun setRecordsServices(services: RecordsServiceFactory) {
        super.setRecordsServices(services)
    }
}
