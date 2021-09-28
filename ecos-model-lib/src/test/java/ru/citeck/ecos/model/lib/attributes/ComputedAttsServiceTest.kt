package ru.citeck.ecos.model.lib.attributes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.api.EcosModelAppApi
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.model.lib.num.repo.NumTemplatesRepo
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class ComputedAttsServiceTest {

    @Test
    fun test() {

        val typeInfoByRef = mutableMapOf<RecordRef, TypeInfo>()
        val numbersByTemplateRef = mutableMapOf<RecordRef, AtomicLong>()

        val services = object : ModelServiceFactory() {
            override fun createNumTemplatesRepo(): NumTemplatesRepo {
                return object : NumTemplatesRepo {
                    override fun getNumTemplate(templateRef: RecordRef): NumTemplateDef {
                        return NumTemplateDef.create {}
                    }
                }
            }
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: RecordRef): TypeInfo? {
                        return typeInfoByRef[typeRef]
                    }
                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
            override fun createEcosModelAppApi(): EcosModelAppApi {
                return object : EcosModelAppApi {
                    override fun getNextNumberForModel(model: ObjectData, templateRef: RecordRef): Long {
                        return numbersByTemplateRef.computeIfAbsent(templateRef) { AtomicLong() }.getAndIncrement()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())

        val attsToStore = services.computedAttsService

        val testTypeRef = RecordRef.valueOf("test-type")

        val value = RecordValue("txt-val", 123, testTypeRef)
        val emptyRes = attsToStore.computeAttsToStore(value, true)
        assertThat(emptyRes.size()).isEqualTo(0)
        val emptyRes2 = attsToStore.computeAttsToStore(value, false)
        assertThat(emptyRes2.size()).isEqualTo(0)

        val typeName = MLText(
            Locale.ENGLISH to "en-type-value",
            Locale.FRANCE to "fr-type-value"
        )

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.id)
            withName(typeName)
        }

        val attsWithName = attsToStore.computeAttsToStore(value, true)
        assertThat(attsWithName.size()).isEqualTo(1)
        assertThat(attsWithName.get("_disp").getAs(MLText::class.java)).isEqualTo(typeName)

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.id)
        }

        val attsWithName2 = attsToStore.computeAttsToStore(value, true)
        assertThat(attsWithName2.size()).isEqualTo(1)
        assertThat(attsWithName2.get("_disp").getAs(MLText::class.java)).isEqualTo(MLText(testTypeRef.id))

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.id)
            withNumTemplateRef(RecordRef.valueOf("counter-ref"))
        }

        val counterAtts = attsToStore.computeAttsToStore(value, true)
        assertThat(counterAtts.size()).isEqualTo(2)
        assertThat(counterAtts.get("_docNum").asLong()).isEqualTo(0L)

        val counterAtts2 = attsToStore.computeAttsToStore(value, true)
        assertThat(counterAtts2.size()).isEqualTo(2)
        assertThat(counterAtts2.get("_docNum").asLong()).isEqualTo(1L)

        val counterAtts3 = attsToStore.computeAttsToStore(value, false)
        assertThat(counterAtts3.size()).isEqualTo(1)
        assertThat(counterAtts3.get("_docNum").isNull()).isTrue

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.id)
        }

        val computedAttDef = ComputedAttDef.create()
            .withStoringType(ComputedAttStoringType.NONE)
            .withType(ComputedAttType.TEMPLATE)
            .withConfig(ObjectData.create("{\"template\":\"value-\${txtField}\"}"))
            .build()
        val computedAttValue = "value-txt-val"

        val createAtt = { id: String, storingType: ComputedAttStoringType ->
            AttributeDef.create()
                .withId(id)
                .withComputed(computedAttDef.copy { withStoringType(storingType) })
                .build()
        }

        typeInfoByRef[testTypeRef] = typeInfoByRef[testTypeRef]!!.copy {
            withModel(
                TypeModelDef.create()
                    .withAttributes(
                        listOf(
                            createAtt("storing-none", ComputedAttStoringType.NONE),
                            createAtt("storing-on-create", ComputedAttStoringType.ON_CREATE),
                            createAtt("storing-on-empty", ComputedAttStoringType.ON_EMPTY),
                            createAtt("storing-on-mutate", ComputedAttStoringType.ON_MUTATE)
                        )
                    )
                    .build()
            )
        }

        val attsForNewRec = attsToStore.computeAttsToStore(value, true)
        assertThat(attsForNewRec.size()).isEqualTo(4)

        assertThat(attsForNewRec.get("storing-none").asText()).isEqualTo("")
        assertThat(attsForNewRec.get("storing-on-create").asText()).isEqualTo(computedAttValue)
        assertThat(attsForNewRec.get("storing-on-empty").asText()).isEqualTo(computedAttValue)
        assertThat(attsForNewRec.get("storing-on-mutate").asText()).isEqualTo(computedAttValue)
    }

    data class RecordValue(
        val txtField: String,
        val numField: Long,
        val ecosType: RecordRef
    )
}
