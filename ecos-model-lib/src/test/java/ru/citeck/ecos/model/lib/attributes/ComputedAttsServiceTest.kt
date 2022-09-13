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
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.entity.EntityRef
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class ComputedAttsServiceTest {

    @Test
    fun test() {

        val typeInfoByRef = mutableMapOf<EntityRef, TypeInfo>()
        val numbersByTemplateRef = mutableMapOf<EntityRef, AtomicLong>()

        val services = object : ModelServiceFactory() {
            override fun createNumTemplatesRepo(): NumTemplatesRepo {
                return object : NumTemplatesRepo {
                    override fun getNumTemplate(templateRef: EntityRef): NumTemplateDef {
                        return NumTemplateDef.create {}
                    }
                }
            }
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        return typeInfoByRef[typeRef]
                    }
                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
            override fun createEcosModelAppApi(): EcosModelAppApi {
                return object : EcosModelAppApi {
                    override fun getNextNumberForModel(model: ObjectData, templateRef: EntityRef): Long {
                        return numbersByTemplateRef.computeIfAbsent(templateRef) { AtomicLong() }.getAndIncrement()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())

        val attsToStore = services.computedAttsService

        val testTypeRef = EntityRef.valueOf("test-type")

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
            withId(testTypeRef.getLocalId())
            withName(typeName)
        }

        val attsWithName = attsToStore.computeAttsToStore(value, true)
        assertThat(attsWithName.size()).isEqualTo(0)
        assertThat(attsToStore.computeDisplayName(value)).isEqualTo(typeName)

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.getLocalId())
        }

        val attsWithName2 = attsToStore.computeAttsToStore(value, true)
        assertThat(attsWithName2.size()).isEqualTo(0)
        assertThat(attsToStore.computeDisplayName(value)).isEqualTo(MLText(testTypeRef.getLocalId()))

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.getLocalId())
            withNumTemplateRef(EntityRef.valueOf("counter-ref"))
        }

        val counterAtts = attsToStore.computeAttsToStore(value, true)
        assertThat(counterAtts.size()).isEqualTo(1)
        assertThat(counterAtts["_docNum"].asLong()).isEqualTo(0L)

        val counterAtts2 = attsToStore.computeAttsToStore(value, true)
        assertThat(counterAtts2.size()).isEqualTo(1)
        assertThat(counterAtts2["_docNum"].asLong()).isEqualTo(1L)

        val counterAtts3 = attsToStore.computeAttsToStore(value, false)
        assertThat(counterAtts3.size()).isEqualTo(0)
        assertThat(counterAtts3["_docNum"].isNull()).isTrue

        typeInfoByRef[testTypeRef] = TypeInfo.create {
            withId(testTypeRef.getLocalId())
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
        assertThat(attsForNewRec.size()).isEqualTo(3)

        assertThat(attsForNewRec["storing-none"].asText()).isEqualTo("")
        assertThat(attsForNewRec["storing-on-create"].asText()).isEqualTo(computedAttValue)
        assertThat(attsForNewRec["storing-on-empty"].asText()).isEqualTo(computedAttValue)
        assertThat(attsForNewRec["storing-on-mutate"].asText()).isEqualTo(computedAttValue)
    }

    data class RecordValue(
        val txtField: String,
        val numField: Long,
        val ecosType: EntityRef
    )
}
