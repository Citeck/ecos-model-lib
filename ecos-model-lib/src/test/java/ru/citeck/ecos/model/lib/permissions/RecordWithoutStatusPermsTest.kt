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

        setTypeRoles(listOf("initiator", "approver"))

        val perms = recordPermsService.getRecordPerms(getRecordRef())!!

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE, "initiator")))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE, "initiator")))

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE, "approver")))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE, "approver")))

        assertTrue(perms.isWriteAllowed(listOf(RoleConstants.ROLE_EVERYONE)))
        assertTrue(perms.isReadAllowed(listOf(RoleConstants.ROLE_EVERYONE)))
    }
}
