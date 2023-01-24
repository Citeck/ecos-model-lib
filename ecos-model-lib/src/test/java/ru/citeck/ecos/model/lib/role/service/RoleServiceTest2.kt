package ru.citeck.ecos.model.lib.role.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.dao.impl.mem.InMemDataRecordsDao
import ru.citeck.ecos.records3.record.mixin.AttMixin
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RoleServiceTest2 {

    companion object {
        private const val SRC_ID = "test-src"
        private const val TYPE_ID = "test-type"
    }

    private lateinit var roleService: RoleService
    private lateinit var records: RecordsService

    @Test
    fun test() {
        val rec0 = records.create(SRC_ID, emptyMap<String, String>())
        val roles = roleService.getCurrentUserRoles(rec0)
        assertThat(roles).isEmpty()

        AuthContext.runAs("user0") {
            assertThat(roleService.getCurrentUserRoles(rec0)).containsExactly("role0")
        }
        AuthContext.runAs("user1") {
            assertThat(roleService.getCurrentUserRoles(rec0)).containsExactly("role1")
        }
        AuthContext.runAs("user2") {
            assertThat(roleService.getCurrentUserRoles(rec0)).isEmpty()
        }
        records.mutateAtt(rec0, "att0", listOf("user2"))
        AuthContext.runAs("user2") {
            assertThat(roleService.getCurrentUserRoles(rec0)).containsExactly("role0")
        }
        val assignees = roleService.getAssignees(rec0, listOf("role0", "role1"))
        assertThat(assignees["role0"]).containsExactlyInAnyOrder("user0", "user2")
        assertThat(assignees["role1"]).containsExactly("user1")
    }

    @BeforeEach
    fun beforeEach() {

        val types = mutableMapOf<String, TypeInfo>()
        val typesRepo = object : TypesRepo {
            override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                return types[typeRef.getLocalId()]
            }
            override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                return emptyList()
            }
        }
        types[TYPE_ID] = TypeInfo.create {
            withId(TYPE_ID)
            withModel(
                TypeModelDef.create()
                    .withRoles(
                        listOf(
                            RoleDef.create()
                                .withId("role0")
                                .withAssignees(listOf("user0"))
                                .withAttributes(listOf("att0"))
                                .build(),
                            RoleDef.create()
                                .withId("role1")
                                .withAssignees(listOf("user1"))
                                .withAttributes(listOf("att1"))
                                .build()
                        )
                    )
                    .build()
            )
        }

        val recordsServices = RecordsServiceFactory()
        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return typesRepo
            }
        }
        services.setRecordsServices(recordsServices)
        val dao = InMemDataRecordsDao(SRC_ID)
        recordsServices.recordsServiceV1.register(dao)
        dao.addAttributesMixin(Mixin())

        this.roleService = services.roleService
        this.records = recordsServices.recordsServiceV1
    }

    class Mixin : AttMixin {
        override fun getAtt(path: String, value: AttValueCtx): Any? {
            return TypeUtils.getTypeRef(TYPE_ID)
        }
        override fun getProvidedAtts() = listOf("_type")
    }
}
