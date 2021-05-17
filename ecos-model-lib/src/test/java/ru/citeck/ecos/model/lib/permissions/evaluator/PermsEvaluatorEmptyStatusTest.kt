package ru.citeck.ecos.model.lib.permissions.evaluator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import kotlin.test.assertTrue

class PermsEvaluatorEmptyStatusTest : PermsEvaluatorTestBase() {

    @Test
    fun testWithoutEmptyStatusInConfig() {

        val roles = listOf("initiator", "approver")
        val statuses = listOf("draft", "approve", "scanning")

        val permsDef = PermissionsDef.create {
            withMatrix(
                mapOf(
                    Pair(
                        "initiator",
                        mapOf(
                            Pair("draft", PermissionLevel.WRITE),
                            Pair("approve", PermissionLevel.READ)
                        )
                    )
                )
            )
        }

        val emptyPerms = getPerms("", roles, statuses, permsDef)

        assertTrue(emptyPerms.isReadAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isReadAllowed(setOf("approver")))
        assertFalse(emptyPerms.isReadAllowed(setOf("unknown")))

        assertFalse(emptyPerms.isWriteAllowed(setOf("initiator")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("approver")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("unknown")))
    }

    @Test
    fun testWithEmptyStatusInConfig() {

        val roles = listOf("initiator", "approver")
        val statuses = listOf("EMPTY", "draft", "approve", "scanning")

        val permsDef = PermissionsDef.create {
            withMatrix(
                mapOf(
                    Pair(
                        "initiator",
                        mapOf(
                            Pair("EMPTY", PermissionLevel.NONE),
                            Pair("draft", PermissionLevel.WRITE),
                            Pair("approve", PermissionLevel.READ)
                        )
                    ),
                    Pair(
                        "approver",
                        mapOf(
                            Pair("EMPTY", PermissionLevel.WRITE),
                            Pair("draft", PermissionLevel.WRITE),
                            Pair("approve", PermissionLevel.READ)
                        )
                    )
                )
            )
        }

        val emptyPerms = getPerms("", roles, statuses, permsDef)

        assertFalse(emptyPerms.isReadAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isReadAllowed(setOf("approver")))
        assertFalse(emptyPerms.isReadAllowed(setOf("unknown")))

        assertFalse(emptyPerms.isWriteAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isWriteAllowed(setOf("approver")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("unknown")))
    }
}
