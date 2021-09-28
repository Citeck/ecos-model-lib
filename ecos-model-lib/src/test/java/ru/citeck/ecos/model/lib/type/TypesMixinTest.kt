package ru.citeck.ecos.model.lib.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.api.records.TypesMixin
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory

class TypesMixinTest {

    @Test
    fun test() {

        val types = listOf(
            CustomTypeDef("contract", "case"),
            CustomTypeDef("currency", "data-list"),
            CustomTypeDef("case", "base"),
            CustomTypeDef("data-list", "base"),
            CustomTypeDef("base", ""),
        )

        val recordsServices = RecordsServiceFactory()
        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: RecordRef): TypeInfo? {
                        val parentRef = types.firstOrNull {
                            it.id == typeRef.id
                        }?.let {
                            TypeUtils.getTypeRef(it.parent)
                        } ?: RecordRef.EMPTY

                        return if (RecordRef.isEmpty(parentRef)) {
                            null
                        } else {
                            TypeInfo.create()
                                .withParentRef(parentRef)
                                .build()
                        }
                    }
                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(recordsServices)

        val records = recordsServices.recordsServiceV1
        val recordsDao = RecordsDaoBuilder.create("test")
            .addRecord("case-rec0", RecordValue("contract"))
            .addRecord("data-list-rec0", RecordValue("currency"))
            .build()
        records.register(recordsDao)

        val typesRecordsBuilder = RecordsDaoBuilder.create("emodel/type")
        types.forEach { typesRecordsBuilder.addRecord(it.id, it) }
        val typesRecords = typesRecordsBuilder.build()
        typesRecords.addAttributesMixin(TypesMixin(services))
        records.register(typesRecords)

        val isSubTypeTest = { recLocalId: String, typeToCheck: String, expected: Boolean ->
            val res = records.getAtt("test@$recLocalId", "_type.isSubTypeOf.$typeToCheck?bool").asJavaObj()
            assertThat(res)
                .describedAs("rec: $recLocalId type: $typeToCheck")
                .isEqualTo(expected)
        }

        isSubTypeTest("case-rec0", "case", true)
        isSubTypeTest("case-rec0", "data-list", false)
        isSubTypeTest("case-rec0", "unknown", false)
        isSubTypeTest("case-rec0", "base", true)
        isSubTypeTest("case-rec0", "currency", false)

        isSubTypeTest("data-list-rec0", "case", false)
        isSubTypeTest("data-list-rec0", "data-list", true)
        isSubTypeTest("data-list-rec0", "unknown", false)
        isSubTypeTest("data-list-rec0", "base", true)
        isSubTypeTest("data-list-rec0", "currency", true)
    }

    data class RecordValue(val type: String) {

        fun getEcosType(): RecordRef {
            return TypeUtils.getTypeRef(type)
        }
    }

    data class CustomTypeDef(val id: String, val parent: String)
}
