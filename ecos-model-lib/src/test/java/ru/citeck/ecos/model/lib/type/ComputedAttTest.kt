package ru.citeck.ecos.model.lib.type

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import kotlin.test.assertEquals

class ComputedAttTest {

    @Test
    fun test() {

        val modelByRef = mutableMapOf<String, TypeModelDef>()

        modelByRef["testparent"] = TypeModelDef.create {
            withAttributes(
                listOf(
                    AttributeDef.create {
                        withId("computedTestAtt")
                    },
                    AttributeDef.create {
                        withId("simpleTestAtt")
                    },
                    AttributeDef.create {
                        withId("computedTestParentAtt")
                        withComputed(
                            ComputedAttDef.create {
                                type = ComputedAttType.SCRIPT
                                config = ObjectData.create(mapOf(Pair("fn", "return 123;")))
                            }
                        )
                    },
                    AttributeDef.create {
                        withId("computedTestAtt")
                        withComputed(
                            ComputedAttDef.create {
                                type = ComputedAttType.SCRIPT
                                config = ObjectData.create(mapOf(Pair("fn", "return 'abc';")))
                            }
                        )
                    },
                    AttributeDef.create {
                        withId("simpleTestAtt")
                    }
                )
            )
        }

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: RecordRef): TypeInfo? {
                        return modelByRef[typeRef.id]?.let { model ->
                            TypeInfo.create {
                                withId(typeRef.id)
                                withModel(model)
                            }
                        }
                    }
                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())

        val typeRef0 = RecordRef.valueOf(TypeUtils.getTypeRef("testparent"))
        val typeRef1 = RecordRef.valueOf(TypeUtils.getTypeRef("otherRef"))

        services.records.recordsServiceV1.register(
            RecordsDaoBuilder.create("test")
                .addRecord("test0record", RecordData(typeRef0))
                .addRecord("test1record", RecordData(typeRef1))
                .build()
        )

        val value0 = services.records.recordsServiceV1.getAtt(RecordRef.valueOf("test@test0record"), "computedTestAtt")
        assertEquals(DataValue.create("abc"), value0)

        val value1 = services.records.recordsServiceV1.getAtt(RecordRef.valueOf("test@test0record"), "computedTestParentAtt?num")
        assertEquals(DataValue.create(123.0), value1)

        val value2 = services.records.recordsServiceV1.getAtt(RecordRef.valueOf("test@test1record"), "computedTestAtt")
        assertEquals(DataValue.NULL, value2)
    }

    class RecordData(private val typeRef: RecordRef) {

        @AttName("_type")
        fun getType(): RecordRef {
            return typeRef
        }
    }
}
