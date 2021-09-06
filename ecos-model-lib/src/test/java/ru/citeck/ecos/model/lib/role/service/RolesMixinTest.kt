package ru.citeck.ecos.model.lib.role.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.api.records.RolesMixin
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao

class RolesMixinTest {

    @Test
    fun test() {
        val testTypeRef = TypeUtils.getTypeRef("test-type")

        val explicitAssignees = listOf("GROUP_EXP_FIRST", "GROUP_EXP_SECOND")
        val roleId = "ROLE_ID"

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {

                    override fun getModel(typeRef: RecordRef): TypeModelDef {
                        if (typeRef == testTypeRef) {
                            return TypeModelDef.create {
                                roles = listOf(
                                    RoleDef.create {
                                        id = roleId
                                        name = MLText(roleId)
                                        assignees = explicitAssignees
                                    }
                                )
                            }
                        }
                        return TypeModelDef.EMPTY
                    }

                    override fun getParent(typeRef: RecordRef): RecordRef {
                        return RecordRef.EMPTY
                    }

                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
        }

        val recId = "rec-0"
        val recRef = RecordRef.create("test", recId)

        val recordsServices = RecordsServiceFactory()
        val records = recordsServices.recordsServiceV1
        services.setRecordsServices(recordsServices)

        val recsDao = RecordsDaoBuilder.create("test")
            .addRecord(recId, TestDto(testTypeRef))
            .build()
        (recsDao as AbstractRecordsDao).addAttributesMixin(RolesMixin(services.roleService))
        records.register(recsDao)

        assertThat(records.getAtt(recRef, "_roles.isCurrentUserMemberOf.$roleId?bool").asBoolean()).isFalse

        AuthContext.runAs("user0", listOf("GROUP_EXP_FIRST")) {
            assertThat(records.getAtt(recRef, "_roles.isCurrentUserMemberOf.$roleId?bool").asBoolean()).isTrue
        }

        assertThat(records.getAtt(recRef, "_roles.assigneesOf.$roleId[]?str").asStrList())
            .containsExactlyElementsOf(explicitAssignees)
    }

    data class TestDto(
        val typeRef: RecordRef
    ) {
        fun getEcosType(): RecordRef {
            return typeRef
        }
    }
}
