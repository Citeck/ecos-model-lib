package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.RecordsServiceFactory
import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder

class RecordPermsServiceTest {

    @Test
    fun test() {

        val types = Array(3) { RecordRef.valueOf(TypeUtils.getTypeRef("type-$it")) }
        val statuses = Array(3) { "status-$it" }

        val sourceId = "test"

        val records = ArrayList<TestDto>()
        for (type in types) {
            for (status in statuses) {
                records.add(TestDto("${type.id}-$status", type, status))
            }
        }

        val daoBuilder = RecordsDaoBuilder.create(sourceId)
        records.forEach { daoBuilder.addRecord(RecordRef.valueOf(it.id), it) }

        val services = ModelServiceFactory()
        services.setRecordsServices(RecordsServiceFactory())
        services.recordsServices.recordsService.register(daoBuilder.build())
    }

    data class TestDto(val id: String, @get:MetaAtt(".type") val type: RecordRef, val _status: String)
}
