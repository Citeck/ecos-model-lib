package ru.citeck.ecos.model.lib.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.api.records.TypesMixin
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.entity.EntityRef

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
                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        val parentRef = types.firstOrNull {
                            it.id == typeRef.getLocalId()
                        }?.let {
                            ModelUtils.getTypeRef(it.parent)
                        } ?: EntityRef.EMPTY

                        return if (EntityRef.isEmpty(parentRef)) {
                            null
                        } else {
                            TypeInfo.create()
                                .withParentRef(parentRef)
                                .build()
                        }
                    }
                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(recordsServices)

        val records = recordsServices.recordsService
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

        fun getEcosType(): EntityRef {
            return ModelUtils.getTypeRef(type)
        }
    }

    data class CustomTypeDef(val id: String, val parent: String)
}
