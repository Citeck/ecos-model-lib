package ru.citeck.ecos.model.lib.type

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttDef
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttType
import ru.citeck.ecos.records3.record.op.atts.service.schema.annotation.AttName
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
                                config = ObjectData.create(mapOf(Pair("script", "return 123;")))
                            }
                        )
                    },
                    AttributeDef.create {
                        withId("computedTestAtt")
                        withComputed(
                            ComputedAttDef.create {
                                type = ComputedAttType.SCRIPT
                                config = ObjectData.create(mapOf(Pair("script", "return 'abc';")))
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

                    override fun getParent(typeRef: RecordRef): RecordRef {
                        return RecordRef.EMPTY
                    }

                    override fun getModel(typeRef: RecordRef): TypeModelDef {
                        return modelByRef[typeRef.id] ?: TypeModelDef.EMPTY
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
