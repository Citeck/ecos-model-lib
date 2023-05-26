package ru.citeck.ecos.model.lib.role.service

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.computed.ComputeDmnDecisionResponseDto
import ru.citeck.ecos.model.lib.role.dto.ComputedRoleType
import ru.citeck.ecos.model.lib.role.dto.RoleComputedDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import ru.citeck.ecos.test.commons.EcosWebAppApiMock
import ru.citeck.ecos.webapp.api.EcosWebAppApi
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.entity.EntityRef
import kotlin.test.assertEquals

class RoleServiceTest {

    @Test
    fun test() {

        val explicitAssignees = listOf("GROUP_EXP_FIRST", "GROUP_EXP_SECOND")
        val assigneesFromDmn = listOf("user1", "user2")
        val roleId = "ROLE_ID"
        val computedRoleId = "computedRoleId"
        val computedRoleDmnId = "computedRoleDmnId"

        val webAppCtxMock = EcosWebAppApiMock(AppName.EPROC)
        webAppCtxMock.webClientExecuteImpl = { _, _, _ ->
            ComputeDmnDecisionResponseDto(
                result = mapOf(
                    "key" to assigneesFromDmn
                )
            )
        }

        val recordsServiceFactory = object : RecordsServiceFactory() {
            override fun getEcosWebAppApi(): EcosWebAppApi {
                return webAppCtxMock
            }
        }

        val services = object : ModelServiceFactory() {
            override fun getEcosWebAppApi(): EcosWebAppApi {
                return webAppCtxMock
            }

            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {

                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {

                        if (typeRef == RecordDto.RECORD_TYPE_REF) {
                            val model = TypeModelDef.create {
                                roles = listOf(
                                    RoleDef.create {
                                        id = roleId
                                        name = MLText(roleId)
                                        assignees = explicitAssignees
                                        withAttributes(listOf("customAtt", "otherAtt"))
                                    },
                                    RoleDef.create {
                                        id = computedRoleId
                                        name = MLText(computedRoleId)
                                        withComputed(
                                            RoleComputedDef.create {
                                                withType(ComputedRoleType.SCRIPT)
                                                withConfig(
                                                    ObjectData.create(
                                                        """
                                                {"fn":"return value.load('customAtt[]?str') || [];"}
                                                        """
                                                    )
                                                )
                                            }
                                        )
                                    },
                                    RoleDef.create {
                                        id = computedRoleDmnId
                                        name = MLText(computedRoleDmnId)
                                        withComputed(
                                            RoleComputedDef.create {
                                                withType(ComputedRoleType.DMN)
                                                withConfig(
                                                    ObjectData.create(
                                                        """
                                                       {
                                                          "decisionRef": "someDecisionRef"
                                                        }
                                                        """
                                                    )
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                            return TypeInfo.create {
                                withId(typeRef.getLocalId())
                                withModel(model)
                            }
                        }
                        return null
                    }

                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
        }

        services.setRecordsServices(recordsServiceFactory)

        val assignees = services.roleService.getAssignees(
            RecordDto(
                RecordDto.CUSTOM_ATT_VALUE_0,
                RecordDto.OTHER_ATT_VALUE_0
            ),
            roleId
        )
        val expected = hashSetOf(
            *RecordDto.CUSTOM_ATT_VALUE_0.toTypedArray(),
            *RecordDto.OTHER_ATT_VALUE_0.toTypedArray(),
            *explicitAssignees.toTypedArray()
        )
        val actual = hashSetOf(*assignees.toTypedArray())

        assertEquals(expected, actual)

        val assignees2 = services.roleService.getAssignees(RecordDto(RecordDto.CUSTOM_ATT_VALUE_1), roleId)
        val expected2 = hashSetOf(*RecordDto.CUSTOM_ATT_VALUE_1.toTypedArray(), *explicitAssignees.toTypedArray())
        val actual2 = hashSetOf(*assignees2.toTypedArray())

        assertEquals(expected2, actual2)

        val assignees3 = services.roleService.getAssignees(RecordDto(RecordDto.CUSTOM_ATT_VALUE_1), computedRoleId)
        val expected3 = hashSetOf(*RecordDto.CUSTOM_ATT_VALUE_1.toTypedArray())
        val actual3 = hashSetOf(*assignees3.toTypedArray())

        assertEquals(expected3, actual3)

        val assigneesFromDmnResult = services.roleService.getAssignees(
            RecordDto(emptyList()),
            computedRoleDmnId
        )

        assertEquals(assigneesFromDmn, assigneesFromDmnResult)
    }

    class RecordDto(
        val customAtt: List<String>,
        val otherAtt: List<String> = emptyList(),
        @AttName("_type")
        val type: EntityRef = RECORD_TYPE_REF
    ) {
        companion object {
            val RECORD_TYPE_REF = ModelUtils.getTypeRef("custom-type")
            val CUSTOM_ATT_VALUE_0 = arrayListOf("first", "second")
            val CUSTOM_ATT_VALUE_1 = arrayListOf("third", "fourth")
            val OTHER_ATT_VALUE_0 = arrayListOf("five", "six")
        }
    }
}
