package ru.citeck.ecos.model.lib.role.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.role.api.records.RolesMixin
import ru.citeck.ecos.model.lib.role.dto.RoleComputedDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao
import ru.citeck.ecos.records3.record.dao.impl.proxy.RecordsDaoProxy
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RolesMixinTest {

    @Test
    fun test() {
        val testTypeRef = TypeUtils.getTypeRef("test-type")

        val explicitAssignees = listOf("GROUP_EXP_FIRST", "GROUP_EXP_SECOND")
        val roleId = "ROLE_ID"

        val roleWithUserId = "roleWithUser"
        val roleWithUserAssignees = listOf("user0")
        val computedRoleId = "computed-role"
        val computedRoleAssignees = listOf("GROUP_first", "GROUP_second")
        val computedRoleConfigJson = """{"fn":"
            |var strVal = value.load('strAtt');
            |return ['${computedRoleAssignees.joinToString("','")}', strVal];
        |"}
        """.trimMargin().replace("\n", " ")

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {

                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        if (typeRef == testTypeRef) {
                            val model = TypeModelDef.create {
                                roles = listOf(
                                    RoleDef.create {
                                        id = roleId
                                        name = MLText(roleId)
                                        assignees = explicitAssignees
                                    },
                                    RoleDef.create {
                                        id = roleWithUserId
                                        name = MLText(roleWithUserId)
                                        assignees = roleWithUserAssignees
                                    },
                                    RoleDef.create {
                                        id = computedRoleId
                                        name = MLText(computedRoleId)
                                        computed = RoleComputedDef.create {
                                            withType(ComputedAttType.SCRIPT)
                                            withConfig(ObjectData.create(computedRoleConfigJson))
                                        }
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

        val recId = "rec-0"
        val sourceId = "test"
        val proxyId = "test-proxy"
        val recRefs = listOf(
            EntityRef.create(sourceId, recId),
            EntityRef.create(proxyId, recId)
        )

        val recordsServices = RecordsServiceFactory()
        val records = recordsServices.recordsServiceV1
        services.setRecordsServices(recordsServices)
        records.register(RecordsDaoProxy(proxyId, sourceId))

        val recsDao = RecordsDaoBuilder.create("test")
            .addRecord(recId, TestDto(testTypeRef))
            .build()
        (recsDao as AbstractRecordsDao).addAttributesMixin(RolesMixin(services.roleService))
        records.register(recsDao)

        for (recRef in recRefs) {

            assertThat(records.getAtt(recRef, "_roles.isCurrentUserMemberOf.$roleId?bool").asBoolean()).isFalse

            AuthContext.runAs("user0", listOf("GROUP_EXP_FIRST")) {
                assertThat(records.getAtt(recRef, "_roles.isCurrentUserMemberOf.$roleId?bool").asBoolean()).isTrue
            }
            AuthContext.runAs("user0", emptyList()) {
                assertThat(
                    records.getAtt(recRef, "_roles.isCurrentUserMemberOf.$roleWithUserId?bool").asBoolean()
                ).isTrue
            }

            assertThat(records.getAtt(recRef, "_roles.assigneesOf.$roleId[]?str").asStrList())
                .containsExactlyElementsOf(explicitAssignees)

            val computedList = records.getAtt(recRef, "_roles.assigneesOf.$computedRoleId[]?str").asStrList()
            assertThat(computedList).containsExactly(*computedRoleAssignees.toTypedArray(), "str-value")
        }
    }

    data class TestDto(
        val typeRef: EntityRef
    ) {
        fun getEcosType(): EntityRef {
            return typeRef
        }

        fun getStrAtt(): String = "str-value"
    }
}
