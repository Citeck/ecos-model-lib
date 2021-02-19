package ru.citeck.ecos.model.lib.type

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttConstraintDef
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.attributes.dto.AttributeType
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.CreateVariantDef
import ru.citeck.ecos.model.lib.type.dto.DocLibDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttDef
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttType
import ru.citeck.ecos.records3.record.op.atts.service.computed.StoringType
import java.util.*
import kotlin.test.assertEquals

class TypeDefTest {

    @Test
    fun dtoTest() {

        val typeDef = TypeDef(
            "test-id",
            MLText.EMPTY.withValue(Locale.ENGLISH, "abc"),
            RecordRef.create("test", "parent", "ref"),
            TypeModelDef(
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
                                withStoringType(StoringType.ON_CREATE)
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
                                withStoringType(StoringType.ON_CREATE)
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
                )
            ),
            DocLibDef.create {
                withEnabled(true)
                withDirTypeRef(RecordRef.create("a", "b", "c"))
                withFileTypeRefs(
                    listOf(
                        RecordRef.valueOf("aa@bb"),
                        RecordRef.valueOf("cc@dd")
                    )
                )
            },
            RecordRef.valueOf("aa/num@template-ref"),
            true,
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
            ),
            ObjectData.create("{\"aaa\":\"bbb\"}")
        )

        val newTypeDef = Json.mapper.read(Json.mapper.toString(typeDef), TypeDef::class.java)

        assertEquals(typeDef, newTypeDef)
    }

    @Test
    fun test() {

        val typeDef = TypeDef.create {
            id = "test"
            docLib = DocLibDef.create { withEnabled(true) }
        }

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeDef(typeRef: RecordRef): TypeDef? {
                        if (typeRef.id == typeDef.id) {
                            return typeDef
                        }
                        return null
                    }
                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())

        val typeRef = TypeUtils.getTypeRef(typeDef.id)
        val docLibDef = services.typeDefService.getDocLib(typeRef)

        assertEquals(1, docLibDef.fileTypeRefs.size)
        assertEquals(typeRef, docLibDef.fileTypeRefs[0])
        assertEquals(TypeUtils.DOCLIB_DEFAULT_DIR_TYPE, docLibDef.dirTypeRef)
    }
}
