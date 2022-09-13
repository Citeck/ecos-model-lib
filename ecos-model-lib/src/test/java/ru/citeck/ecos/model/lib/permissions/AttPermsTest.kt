package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import ru.citeck.ecos.webapp.api.entity.EntityRef
import kotlin.test.assertEquals

class AttPermsTest {

    @Test
    fun test() {

        val permsDef = PermissionsDef.create {
            withMatrix(
                mapOf(
                    Pair(
                        "initiator",
                        mapOf(
                            Pair("draft", PermissionLevel.WRITE),
                            Pair("approve", PermissionLevel.READ),
                            Pair("scanning", PermissionLevel.NONE)
                        )
                    ),
                    Pair(
                        "approver",
                        mapOf(
                            Pair("draft", PermissionLevel.READ),
                            Pair("approve", PermissionLevel.WRITE),
                            Pair("scanning", PermissionLevel.NONE)
                        )
                    ),
                    Pair(
                        "scan-man",
                        mapOf(
                            Pair("draft", PermissionLevel.NONE),
                            Pair("approve", PermissionLevel.NONE),
                            Pair("scanning", PermissionLevel.WRITE)
                        )
                    )
                )
            )
        }

        val typePermsDef = TypePermsDef.create {
            withAttributes(
                mapOf(
                    Pair("att0", permsDef),
                    Pair("att1.att2", permsDef)
                )
            )
        }

        val modelServiceFactory = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {

                return object : TypesRepo {

                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {

                        if ("test-type" == typeRef.getLocalId()) {
                            val model = TypeModelDef.create {
                                withAttributes(
                                    listOf(
                                        AttributeDef.create {
                                            withId("att0")
                                        },
                                        AttributeDef.create {
                                            withId("att1.att2")
                                        },
                                        AttributeDef.create {
                                            withId("known_without_matrix")
                                        }
                                    )
                                )
                                withStatuses(
                                    listOf(
                                        StatusDef.create { withId("draft") },
                                        StatusDef.create { withId("approve") },
                                        StatusDef.create { withId("scanning") }
                                    )
                                )
                                withRoles(
                                    listOf(
                                        RoleDef.create { withId("initiator") },
                                        RoleDef.create { withId("approver") },
                                        RoleDef.create { withId("scan-man") }
                                    )
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
            override fun createPermissionsRepo(): PermissionsRepo {
                return object : PermissionsRepo {
                    override fun getPermissionsForType(typeRef: EntityRef): TypePermsDef? {
                        if ("test-type" == typeRef.getLocalId()) {
                            return typePermsDef
                        }
                        return null
                    }
                }
            }
        }
        modelServiceFactory.setRecordsServices(RecordsServiceFactory())

        modelServiceFactory.records.recordsServiceV1.register(
            RecordsDaoBuilder.create("test")
                .addRecord("test-draft", TestDto("value0", "value1", "draft"))
                .addRecord("test-approve", TestDto("value0", "value1", "approve"))
                .addRecord("test-scanning", TestDto("value0", "value1", "scanning"))
                .build()
        )

        val attPerms = modelServiceFactory.recordPermsService.getRecordAttsPerms(EntityRef.valueOf("test@test-draft"))!!
        val perms = attPerms.getPermissions("att0")

        assertEquals(true, perms.isReadAllowed(listOf("initiator")))
        assertEquals(true, perms.isWriteAllowed(listOf("initiator")))
        assertEquals(true, perms.isReadAllowed(listOf("approver")))
        assertEquals(false, perms.isWriteAllowed(listOf("approver")))

        val notExistsAttPerms = attPerms.getPermissions("att-unknown")

        assertEquals(true, notExistsAttPerms.isReadAllowed(listOf("initiator")))
        assertEquals(true, notExistsAttPerms.isWriteAllowed(listOf("initiator")))
        assertEquals(true, notExistsAttPerms.isReadAllowed(listOf("approver")))
        assertEquals(true, notExistsAttPerms.isWriteAllowed(listOf("approver")))

        val notExistsInnerAtt = attPerms.getPermissions("att0.unknown-att")

        assertEquals(true, notExistsInnerAtt.isReadAllowed(listOf("initiator")))
        assertEquals(true, notExistsInnerAtt.isWriteAllowed(listOf("initiator")))
        assertEquals(true, notExistsInnerAtt.isReadAllowed(listOf("approver")))
        assertEquals(true, notExistsInnerAtt.isWriteAllowed(listOf("approver")))

        val existsInnerAtt = attPerms.getPermissions("att1.att2")

        assertEquals(true, existsInnerAtt.isReadAllowed(listOf("initiator")))
        assertEquals(true, existsInnerAtt.isWriteAllowed(listOf("initiator")))
        assertEquals(true, existsInnerAtt.isReadAllowed(listOf("approver")))
        assertEquals(false, existsInnerAtt.isWriteAllowed(listOf("approver")))

        val knownAttWithoutMatrix = attPerms.getPermissions("known_without_matrix")

        assertEquals(true, knownAttWithoutMatrix.isReadAllowed(listOf("initiator")))
        assertEquals(false, knownAttWithoutMatrix.isWriteAllowed(listOf("initiator")))
        assertEquals(true, knownAttWithoutMatrix.isReadAllowed(listOf("approver")))
        assertEquals(false, knownAttWithoutMatrix.isWriteAllowed(listOf("approver")))

        // allow to edit attributes outside of model
        val unknownAttWithoutMatrix = attPerms.getPermissions("unknown_without_matrix")

        assertEquals(true, unknownAttWithoutMatrix.isReadAllowed(listOf("initiator")))
        assertEquals(true, unknownAttWithoutMatrix.isWriteAllowed(listOf("initiator")))
        assertEquals(true, unknownAttWithoutMatrix.isReadAllowed(listOf("approver")))
        assertEquals(true, unknownAttWithoutMatrix.isWriteAllowed(listOf("approver")))
    }

    data class TestDto(
        val att0: String,
        val att1: String,
        private val status: String
    ) {
        @AttName("_type")
        fun getType(): EntityRef {
            return TypeUtils.getTypeRef("test-type")
        }
        @AttName("_status")
        fun getStatus(): String {
            return status
        }
    }
}
