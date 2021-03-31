package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.testutils.PermsTestBase
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordWithoutStatusPermsTest : PermsTestBase() {

    @Test
    fun testWithEmptyStatus() {

        setPermissions(
            TypePermsDef.create {
                withPermissions(
                    PermissionsDef.create {
                        withMatrix(
                            mapOf(
                                "initiator" to mapOf(
                                    "EMPTY" to PermissionLevel.WRITE
                                ),
                                "approver" to mapOf(
                                    "EMPTY" to PermissionLevel.READ
                                )
                            )
                        )
                    }
                )
            }
        )

        setTypeRoles(listOf("initiator", "approver"))

        val perms = recordPermsService.getRecordPerms(getRecordRef())!!

        assertTrue(perms.isWriteAllowed(listOf("initiator")))
        assertTrue(perms.isReadAllowed(listOf("initiator")))

        assertFalse(perms.isWriteAllowed(listOf("approver")))
        assertTrue(perms.isReadAllowed(listOf("approver")))

        assertFalse(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE)))
    }

    @Test
    fun testAnyRole() {

        setPermissions(
            TypePermsDef.create {
                withPermissions(
                    PermissionsDef.create {
                        withMatrix(
                            mapOf(
                                RoleConstants.ROLE_EVERYONE to mapOf(
                                    "ANY" to PermissionLevel.WRITE
                                )
                            )
                        )
                    }
                )
            }
        )

        setTypeRoles(listOf("initiator", "approver", RoleConstants.ROLE_EVERYONE))

        val perms = recordPermsService.getRecordPerms(getRecordRef())!!

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE, "initiator")))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE, "initiator")))

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE, "approver")))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE, "approver")))

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE)))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE)))
    }

    @Test
    fun testWithPartialMatrix() {

        val testWith = { status: String, roles: List<String>, expectedLevel: PermissionLevel ->

            setRecordStatus(status)
            val perms = recordPermsService.getRecordPerms(getRecordRef())!!

            if (expectedLevel == PermissionLevel.WRITE) {
                assertTrue(perms.isWriteAllowed(roles))
            } else {
                assertFalse(perms.isWriteAllowed(roles))
            }

            if (expectedLevel == PermissionLevel.WRITE || expectedLevel == PermissionLevel.READ) {
                assertTrue(perms.isReadAllowed(roles))
            } else {
                assertFalse(perms.isReadAllowed(roles))
            }
        }

        setPermissions(
            TypePermsDef.create {
                withPermissions(
                    PermissionsDef.create {
                        withMatrix(
                            mapOf(
                                "initiator" to mapOf(
                                    "status0" to PermissionLevel.WRITE,
                                    "statusOutOfType" to PermissionLevel.WRITE
                                ),
                                "customRoleOutOfType" to mapOf(
                                    "status0" to PermissionLevel.WRITE,
                                    "statusOutOfType" to PermissionLevel.WRITE
                                )
                            )
                        )
                    }
                )
            }
        )

        setTypeRoles(listOf("initiator", "approver"))
        setTypeStatuses(listOf("status0", "statusOutOfMtx"))

        // typeStatus: OK, mtxStatus: OK, typeRole: OK, mtxRole: OK
        testWith("status0", listOf("initiator"), PermissionLevel.WRITE)
        // typeStatus: OK, mtxStatus: OK, typeRole: OK, mtxRole: MISSING
        testWith("status0", listOf("approver"), PermissionLevel.READ)
        // typeStatus: OK, mtxStatus: OK, typeRole: MISSING, mtxRole: OK
        testWith("status0", listOf("customRoleOutOfType"), PermissionLevel.NONE)
        // typeStatus: OK, mtxStatus: OK, typeRole: MISSING, mtxRole: MISSING
        testWith("status0", listOf("unknown"), PermissionLevel.NONE)

        // typeStatus: OK, mtxStatus: MISSING, typeRole: OK, mtxRole: OK
        testWith("statusOutOfMtx", listOf("initiator"), PermissionLevel.READ)
        // typeStatus: OK, mtxStatus: MISSING, typeRole: OK, mtxRole: MISSING
        testWith("statusOutOfMtx", listOf("approver"), PermissionLevel.READ)
        // typeStatus: OK, mtxStatus: MISSING, typeRole: MISSING, mtxRole: OK
        testWith("statusOutOfMtx", listOf("customRoleOutOfType"), PermissionLevel.NONE)
        // typeStatus: OK, mtxStatus: MISSING, typeRole: MISSING, mtxRole: MISSING
        testWith("statusOutOfMtx", listOf("unknown"), PermissionLevel.NONE)

        // typeStatus: MISSING, mtxStatus: OK, typeRole: OK, mtxRole: OK
        testWith("statusOutOfType", listOf("initiator"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: OK, typeRole: OK, mtxRole: MISSING
        testWith("statusOutOfType", listOf("approver"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: OK, typeRole: MISSING, mtxRole: OK
        testWith("statusOutOfType", listOf("customRoleOutOfType"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: OK, typeRole: MISSING, mtxRole: MISSING
        testWith("statusOutOfType", listOf("unknown"), PermissionLevel.NONE)

        // typeStatus: MISSING, mtxStatus: MISSING, typeRole: OK, mtxRole: OK
        testWith("unknown", listOf("initiator"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: MISSING, typeRole: OK, mtxRole: MISSING
        testWith("unknown", listOf("approver"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: MISSING, typeRole: MISSING, mtxRole: OK
        testWith("unknown", listOf("customRoleOutOfType"), PermissionLevel.NONE)
        // typeStatus: MISSING, mtxStatus: MISSING, typeRole: MISSING, mtxRole: MISSING
        testWith("unknown", listOf("unknown"), PermissionLevel.NONE)
    }
}
