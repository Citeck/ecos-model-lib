package ru.citeck.ecos.model.lib.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.model.lib.attributes.dto.AttConstraintDef
import ru.citeck.ecos.model.lib.attributes.dto.AttIndexDef
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.attributes.dto.AttributeType
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.procstages.dto.ProcStageDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.*
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.entity.EntityRef
import ru.citeck.ecos.webapp.api.entity.toEntityRef
import java.util.*
import kotlin.test.assertEquals

class TypeDefTest {

    @Test
    fun modelDefTest() {

        val modelDef = TypeModelDef.create()
            .withStatuses(
                listOf(
                    StatusDef.create().withId("status-0").withName(MLText("status-0-name-0")).build(),
                    StatusDef.create().withId("status-1").withName(MLText("status-1-name-1")).build(),
                    StatusDef.create().withId("status-2").withName(MLText("status-2-name-2")).build(),
                    StatusDef.create().withId("status-3").withName(MLText("status-3-name-3")).build(),
                    StatusDef.create().withId("status-1").withName(MLText("status-1-name-4")).build()
                )
            ).build()

        assertThat(modelDef.statuses.map { it.id to it.name.getClosestValue() }).containsExactly(
            "status-0" to "status-0-name-0",
            "status-1" to "status-1-name-4",
            "status-2" to "status-2-name-2",
            "status-3" to "status-3-name-3"
        )

        val modelDef2 = TypeModelDef.create()
            .withStatuses(
                listOf(
                    StatusDef.create().withId("status-0").build(),
                    StatusDef.create().build()
                )
            )
            .withAttributes(
                listOf(
                    AttributeDef.create().withId("att-0").build(),
                    AttributeDef.create().build()
                )
            )
            .withStages(
                listOf(
                    ProcStageDef.create().withId("stage-0").build(),
                    ProcStageDef.create().withId("stage-1").withName(MLText("stage-1")).build(),
                    ProcStageDef.create().withName(MLText("stage-2")).build()
                )
            )
            .build()

        assertThat(modelDef2.statuses).hasSize(1)
        assertThat(modelDef2.statuses[0]).isEqualTo(StatusDef.create().withId("status-0").build())

        assertThat(modelDef2.attributes).hasSize(1)
        assertThat(modelDef2.attributes[0]).isEqualTo(AttributeDef.create().withId("att-0").build())

        assertThat(modelDef2.stages).hasSize(3)

        val stageWithGeneratedId = modelDef2.stages.find { it.id != "stage-0" && it.id != "stage-1" }!!
        assertThat(stageWithGeneratedId.id).isNotBlank
        assertThat(stageWithGeneratedId.name.getClosestValue()).isEqualTo("stage-2")

        assertThat(modelDef2.stages).containsExactly(
            ProcStageDef.create().withId("stage-0").build(),
            ProcStageDef.create().withId("stage-1").withName(MLText("stage-1")).build(),
            stageWithGeneratedId
        )

        val modelDef3 = Json.mapper.readNotNull(Json.mapper.toStringNotNull(modelDef2), TypeModelDef::class.java)
        assertThat(modelDef3.stages).containsExactly(
            ProcStageDef.create().withId("stage-0").build(),
            ProcStageDef.create().withId("stage-1").withName(MLText("stage-1")).build(),
            stageWithGeneratedId
        )
        assertThat(modelDef3).isEqualTo(modelDef2)
    }

    @Test
    fun indexTest() {

        val index = AttIndexDef.create()
            .withEnabled(true)
            .build()

        assertThat(Json.mapper.toNonDefaultJson(index).toString()).isEqualTo("{\"enabled\":true}")

        val att = AttributeDef.create()
            .withId("id")
            .withIndex(index)
            .build()

        assertThat(Json.mapper.toNonDefaultJson(att).toString()).isEqualTo("{\"id\":\"id\",\"index\":{\"enabled\":true}}")

        val typeInfo = TypeInfo.create()
            .withModel(
                TypeModelDef.create()
                    .withSystemAttributes(listOf(att))
                    .build()
            ).build()

        println(Json.mapper.toNonDefaultJson(typeInfo))
    }

    @Test
    fun typeInfoTest() {

        val typeInfo = TypeInfo.create {
            withId("some-id")
            withName(
                MLText(
                    Locale.ENGLISH to "en",
                    Locale.FRANCE to "fr"
                )
            )
            withSourceId("source-id")
            withNumTemplateRef(EntityRef.create("abc", "def", "hig"))
            withParentRef(EntityRef.create("abc", "def", "hig"))
            withDispNameTemplate(
                MLText(
                    Locale.ENGLISH to "disp-en",
                    Locale.FRANCE to "disp-fr"
                )
            )
            withAspects(
                listOf(
                    TypeAspectDef.create()
                        .withRef("emodel/aspect@some-aspect".toEntityRef())
                        .withConfig(ObjectData.create("""{"aa":"bb"}"""))
                        .build(),
                    TypeAspectDef.create()
                        .withRef("emodel/aspect@some-aspect2".toEntityRef())
                        .withConfig(ObjectData.create("""{"cc":"dd"}"""))
                        .build()
                )
            )
            withModel(
                TypeModelDef.create()
                    .withAttributes(
                        listOf(
                            AttributeDef.create {
                                withId("abc")
                                withType(AttributeType.AUTHORITY)
                                withMandatory(true)
                                withMultiple(true)
                                withConfig(ObjectData.create("""{"aa":"bb"}"""))
                                withComputed(
                                    ComputedAttDef.create {
                                        withId("com-id")
                                        withType(ComputedAttType.SCRIPT)
                                        withConfig(ObjectData.create("""{"cc":"dd"}"""))
                                        withStoringType(ComputedAttStoringType.ON_CREATE)
                                    }
                                )
                            }
                        )
                    )
                    .withSystemAttributes(
                        listOf(
                            AttributeDef.create {
                                withId("abc")
                                withType(AttributeType.AUTHORITY)
                                withMandatory(true)
                                withMultiple(true)
                                withConfig(ObjectData.create("""{"aa":"bb"}"""))
                                withComputed(
                                    ComputedAttDef.create {
                                        withId("com-id")
                                        withType(ComputedAttType.SCRIPT)
                                        withConfig(ObjectData.create("""{"cc":"dd"}"""))
                                        withStoringType(ComputedAttStoringType.ON_CREATE)
                                    }
                                )
                            }
                        )
                    )
                    .build()
            )
        }
        val typeInfoFromJson = Json.mapper.convert(Json.mapper.toJson(typeInfo), TypeInfo::class.java)
        assertThat(typeInfoFromJson).isEqualTo(typeInfo)
        val typeInfoFromCopy = typeInfo.copy().build()
        assertThat(typeInfoFromCopy).isEqualTo(typeInfo)
    }

    @Test
    fun computedAttTest() {

        val computedAtt = ComputedAttDef.create()
            .withType(ComputedAttType.SCRIPT)
            .withConfig(ObjectData.create("""{"fn":"return 'abc';"}"""))
            .withStoringType(ComputedAttStoringType.ON_CREATE)
            .build()

        val services = RecordsServiceFactory()
        val records = services.recordsService
        val computedAttFromRecords = records.getAtts(computedAtt, ComputedAttDef::class.java)

        assertThat(computedAttFromRecords).isEqualTo(computedAtt)

        val modelDef = TypeModelDef.create()
            .withAttributes(
                listOf(
                    AttributeDef.create()
                        .withId("some-id")
                        .withComputed(computedAtt)
                        .build()
                )
            )
            .build()

        val computed = records.getAtt(modelDef, "attributes[].computed?json")
        assertThat(computed).isEqualTo(DataValue.create(listOf(computedAtt)))

        val modelDefFromRecords = records.getAtts(modelDef, TypeModelDef::class.java)
        assertThat(modelDefFromRecords).isEqualTo(modelDef)
    }

    @Test
    fun dtoTest() {

        val typeModelDef = TypeModelDef(
            listOf(
                RoleDef.create {
                    withId("test-role")
                    withAttribute("Attribute")
                    withAssignees(listOf("One", "Two"))
                },
                RoleDef.create {
                    withId("test-role2")
                    withAttribute("Attribute2")
                    withAssignees(listOf("One2", "Two2"))
                }
            ),
            listOf(
                StatusDef.create {
                    withId("status123")
                    withName(MLText.EMPTY.withValue(Locale.ENGLISH, "status-name"))
                    withConfig(ObjectData.create("""{"aa":"bb","cc":"dd"}"""))
                },
                StatusDef.create {
                    withId("status1234567")
                    withName(MLText.EMPTY.withValue(Locale.ENGLISH, "status-name22"))
                    withConfig(ObjectData.create("""{"aa3":"bb3","cc2":"dd2"}"""))
                }
            ),
            listOf(
                ProcStageDef.create {
                    withId("custom")
                    withStatuses(listOf("status123"))
                }
            ),
            listOf(
                AttributeDef.create {
                    withId("test-att")
                    withComputed(
                        ComputedAttDef.create {
                            withType(ComputedAttType.VALUE)
                            withConfig(ObjectData.create("""{"aa3":"bb3","cc2":"dd2"}"""))
                            withStoringType(ComputedAttStoringType.ON_CREATE)
                        }
                    )
                    withName(MLText("Content"))
                    withType(AttributeType.ASSOC)
                    withConfig(ObjectData.create("""{"aa3":"bb3","cc2":"dd2"}"""))
                    withMultiple(true)
                    withMandatory(true)
                    withConstraint(
                        AttConstraintDef(
                            "test-constraint",
                            ObjectData.create("""{"aa":"bb"}""")
                        )
                    )
                },
                AttributeDef.create {
                    withId("test-att22")
                    withComputed(
                        ComputedAttDef.create {
                            withType(ComputedAttType.VALUE)
                            withConfig(ObjectData.create("""{"a1a3":"bb13","c1c2":"dd12"}"""))
                            withStoringType(ComputedAttStoringType.ON_CREATE)
                        }
                    )
                    withName(MLText("Content"))
                    withType(AttributeType.ASSOC)
                    withConfig(ObjectData.create("""{"a1a3":"b1b3","c1c2":"d1d2"}"""))
                    withMultiple(true)
                    withMandatory(true)
                    withConstraint(
                        AttConstraintDef(
                            "test-constraint2",
                            ObjectData.create("""{"aa3":"b4b"}""")
                        )
                    )
                }
            ),
            listOf(
                AttributeDef.create {
                    withId("test-att223")
                    withComputed(
                        ComputedAttDef.create {
                            withType(ComputedAttType.VALUE)
                            withConfig(ObjectData.create("""{"a1a3":"bb13","c1c2":"dd12"}"""))
                            withStoringType(ComputedAttStoringType.ON_CREATE)
                        }
                    )
                    withName(MLText("Content"))
                    withType(AttributeType.ASSOC)
                    withConfig(ObjectData.create("""{"a1a3":"b1b3","c1c2":"d1d2"}"""))
                    withMultiple(true)
                    withMandatory(true)
                    withConstraint(
                        AttConstraintDef(
                            "test-constraint2",
                            ObjectData.create("""{"aa3":"b4b"}""")
                        )
                    )
                    withIndex(
                        AttIndexDef.create()
                            .withEnabled(true)
                            .build()
                    )
                }
            )
        )

        val newTypeDef = Json.mapper.read(Json.mapper.toString(typeModelDef), TypeModelDef::class.java)
        assertEquals(typeModelDef, newTypeDef)

        val docLib = DocLibDef.create {
            withEnabled(true)
            withDirTypeRef(EntityRef.create("a", "b", "c"))
            withFileTypeRefs(
                listOf(
                    EntityRef.valueOf("aa@bb"),
                    EntityRef.valueOf("cc@dd")
                )
            )
        }

        val newDocLib = Json.mapper.read(Json.mapper.toString(docLib), DocLibDef::class.java)
        assertEquals(docLib, newDocLib)

        val createVariants = ArrayList(
            listOf(
                CreateVariantDef.create {
                    withId("create-test-id")
                    withName(MLText("asadasd"))
                    withSourceId("sourceid")
                    withTypeRef(EntityRef.valueOf("aa/bb@cc"))
                    withFormRef(EntityRef.valueOf("dd/ee@ff"))
                    withPostActionRef(EntityRef.valueOf("gg/hh@ii"))
                    withFormOptions(ObjectData.create("""{"aa":"bb"}"""))
                    withAttributes(ObjectData.create("""{"aa":"bb"}"""))
                    withProperties(ObjectData.create("""{"aa":"bb"}"""))
                },
                CreateVariantDef.create {
                    withId("create-test-id22")
                    withName(MLText("asadasd22"))
                    withSourceId("sourceid22")
                    withTypeRef(EntityRef.valueOf("aa/bb@cc22"))
                    withFormRef(EntityRef.valueOf("dd/ee@ff22"))
                    withPostActionRef(EntityRef.valueOf("gg/hh@ii22"))
                    withFormOptions(ObjectData.create("""{"aa":"bb22"}"""))
                    withAttributes(ObjectData.create("""{"aa":"bb24"}"""))
                    withProperties(ObjectData.create("""{"aa":"bb3"}"""))
                }
            )
        )

        val newCreateVariants = ArrayList(
            Json.mapper.read(
                Json.mapper.toString(createVariants),
                DataValue::class.java
            )!!.toList(CreateVariantDef::class.java)
        )

        assertEquals(createVariants, newCreateVariants)

        val records = RecordsServiceFactory().recordsService
        val newTypeModelDef = records.getAtts(typeModelDef, TypeModelDef::class.java)
        assertThat(newTypeModelDef).isEqualTo(typeModelDef)
    }
}
