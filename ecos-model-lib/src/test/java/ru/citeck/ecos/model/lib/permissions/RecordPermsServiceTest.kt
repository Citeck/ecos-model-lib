package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RecordPermsServiceTest {

    @Test
    fun test() {

        val types = Array(3) { EntityRef.valueOf(ModelUtils.getTypeRef("type-$it")) }
        val statuses = Array(3) { "status-$it" }

        val sourceId = "test"

        val records = ArrayList<TestDto>()
        for (type in types) {
            for (status in statuses) {
                records.add(TestDto("${type.getLocalId()}-$status", type, status))
            }
        }

        val daoBuilder = RecordsDaoBuilder.create(sourceId)
        records.forEach { daoBuilder.addRecord(it.id, it) }

        val services = ModelServiceFactory()
        services.setRecordsServices(RecordsServiceFactory())
        services.records.recordsServiceV1.register(daoBuilder.build())
    }

    data class TestDto(val id: String, @AttName("?type") val type: EntityRef, val _status: String)
}
