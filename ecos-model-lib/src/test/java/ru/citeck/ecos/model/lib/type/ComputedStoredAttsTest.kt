package ru.citeck.ecos.model.lib.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.resolver.AttContext
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.request.RequestContext
import ru.citeck.ecos.webapp.api.entity.EntityRef

class ComputedStoredAttsTest {

    companion object {
        const val TEST_TYPE_ID = "test-type"
        const val SIMPLE_ATT_ID = "simpleAtt"
        const val SIMPLE_ATT_VALUE = "simpleAttValue"

        const val ON_EMPTY_ATT = "onEmpty"
        const val ON_MUTATE_ATT = "onMutate"
        const val ON_CREATE_ATT = "onCreate"
        const val NONE_ATT = "noneAtt"
    }

    private lateinit var services: ModelServiceFactory

    @BeforeEach
    fun beforeEach() {

        val computedTemplateConfig = ObjectData.create()
            .set("template", "\${$SIMPLE_ATT_ID}")

        val atts = listOf(
            AttributeDef.create {
                withId(SIMPLE_ATT_ID)
            },
            AttributeDef.create {
                withId(ON_EMPTY_ATT)
                withComputed(
                    ComputedAttDef.create {
                        this.withStoringType(ComputedAttStoringType.ON_EMPTY)
                        this.withType(ComputedAttType.TEMPLATE)
                        this.withConfig(computedTemplateConfig)
                    }
                )
            },
            AttributeDef.create {
                withId(ON_CREATE_ATT)
                withComputed(
                    ComputedAttDef.create {
                        this.withStoringType(ComputedAttStoringType.ON_CREATE)
                        this.withType(ComputedAttType.TEMPLATE)
                        this.withConfig(computedTemplateConfig)
                    }
                )
            },
            AttributeDef.create {
                withId(ON_MUTATE_ATT)
                withComputed(
                    ComputedAttDef.create {
                        this.withStoringType(ComputedAttStoringType.ON_MUTATE)
                        this.withType(ComputedAttType.TEMPLATE)
                        this.withConfig(computedTemplateConfig)
                    }
                )
            },
            AttributeDef.create {
                withId(NONE_ATT)
                withComputed(
                    ComputedAttDef.create {
                        this.withStoringType(ComputedAttStoringType.NONE)
                        this.withType(ComputedAttType.TEMPLATE)
                        this.withConfig(computedTemplateConfig)
                    }
                )
            }
        )

        val type = TypeInfo.create {
            withId(TEST_TYPE_ID)
            withModel(TypeModelDef.create { withAttributes(atts) })
        }

        services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        return if (typeRef.getLocalId() != type.id) {
                            null
                        } else {
                            type
                        }
                    }
                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())
    }

    @Test
    fun test() {

        val record = RecordData()
        services.records.recordsServiceV1.register(
            RecordsDaoBuilder.create("test")
                .addRecord("test", record)
                .build()
        )
        val testRef = EntityRef.create("test", "test")

        val assertAtts = { onEmptyValue: String?, isNewRec: Boolean ->
            record.onEmptyVal = onEmptyValue
            listOf(record, testRef).forEach { recForCompute ->

                val data = RequestContext.doWithCtx {
                    AttContext.doWithCtx(services.records) {
                        services.computedAttsService.computeAttsToStore(recForCompute, isNewRec)
                    }
                }
                val checkValue = { att: String, exists: Boolean ->

                    val checkValueMsg = "onEmptyVal: $onEmptyValue " +
                        "| isNewRec: $isNewRec " +
                        "| rec: $recForCompute " +
                        "| recType: ${recForCompute::class.java.simpleName} " +
                        "| att: $att " +
                        "| exists: $exists"

                    if (!exists) {
                        assertThat(data.has(att))
                            .describedAs(checkValueMsg)
                            .isFalse
                    } else {
                        assertThat(data.get(att).asText())
                            .describedAs(checkValueMsg)
                            .isEqualTo(SIMPLE_ATT_VALUE)
                    }
                }
                checkValue(ON_EMPTY_ATT, onEmptyValue.isNullOrEmpty())
                checkValue(ON_MUTATE_ATT, true)
                checkValue(ON_CREATE_ATT, isNewRec)
                checkValue(NONE_ATT, false)
            }
        }

        assertAtts(null, false)
        assertAtts(null, true)
        assertAtts("", false)
        assertAtts("", true)
        assertAtts("abc", false)
        assertAtts("abc", true)
    }

    class RecordData(var onEmptyVal: String? = null) : AttValue {

        override fun getAtt(name: String): Any? {
            return when (name) {
                SIMPLE_ATT_ID -> SIMPLE_ATT_VALUE
                ON_EMPTY_ATT -> onEmptyVal
                else -> null
            }
        }

        override fun getType(): EntityRef {
            return ModelUtils.getTypeRef(TEST_TYPE_ID)
        }
    }
}
