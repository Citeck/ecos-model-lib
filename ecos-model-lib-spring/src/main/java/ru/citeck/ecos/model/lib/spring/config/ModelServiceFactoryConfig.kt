package ru.citeck.ecos.model.lib.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeDefService
import ru.citeck.ecos.records3.RecordsServiceFactory

@Configuration
open class ModelServiceFactoryConfig : ModelServiceFactory() {

    private lateinit var repo: TypesRepo

    @Bean
    override fun createTypeDefService(): TypeDefService {
        return super.createTypeDefService()
    }

    override fun createTypesRepo(): TypesRepo {
        return repo
    }

    @Autowired
    fun setTypesRepo(repo: TypesRepo) {
        this.repo = repo
    }

    @Autowired
    override fun setRecordsServices(services: RecordsServiceFactory) {
        super.setRecordsServices(services)
    }
}
