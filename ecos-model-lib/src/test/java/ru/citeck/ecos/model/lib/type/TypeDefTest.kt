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
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.CreateVariantDef
import ru.citeck.ecos.model.lib.type.dto.DocLibDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import java.util.*
import kotlin.test.assertEquals

class TypeDefTest {

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
            withNumTemplateRef(RecordRef.create("abc", "def", "hig"))
            withParentRef(RecordRef.create("abc", "def", "hig"))
            withDispNameTemplate(
                MLText(
                    Locale.ENGLISH to "disp-en",
                    Locale.FRANCE to "disp-fr"
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
        val records = services.recordsServiceV1
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
            withDirTypeRef(RecordRef.create("a", "b", "c"))
            withFileTypeRefs(
                listOf(
                    RecordRef.valueOf("aa@bb"),
                    RecordRef.valueOf("cc@dd")
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
                    withTypeRef(RecordRef.valueOf("aa/bb@cc"))
                    withFormRef(RecordRef.valueOf("dd/ee@ff"))
                    withPostActionRef(RecordRef.valueOf("gg/hh@ii"))
                    withFormOptions(ObjectData.create("""{"aa":"bb"}"""))
                    withAttributes(ObjectData.create("""{"aa":"bb"}"""))
                    withProperties(ObjectData.create("""{"aa":"bb"}"""))
                },
                CreateVariantDef.create {
                    withId("create-test-id22")
                    withName(MLText("asadasd22"))
                    withSourceId("sourceid22")
                    withTypeRef(RecordRef.valueOf("aa/bb@cc22"))
                    withFormRef(RecordRef.valueOf("dd/ee@ff22"))
                    withPostActionRef(RecordRef.valueOf("gg/hh@ii22"))
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

        val records = RecordsServiceFactory().recordsServiceV1
        val newTypeModelDef = records.getAtts(typeModelDef, TypeModelDef::class.java)
        assertThat(newTypeModelDef).isEqualTo(typeModelDef)
    }
}
