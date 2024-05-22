package ru.citeck.ecos.model.lib.permissions.evaluator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import kotlin.test.assertTrue

class PermsEvaluatorEmptyStatusTest : PermsEvaluatorTestBase() {

    @Test
    fun testWithoutEmptyStatusInConfig() {

        setRoles(listOf("initiator", "approver"))
        setStatuses(listOf("draft", "approve", "scanning"))

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

        setStatusForRecord("")
        val emptyPerms = getPerms(permsDef)

        assertTrue(emptyPerms.isReadAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isReadAllowed(setOf("approver")))
        assertFalse(emptyPerms.isReadAllowed(setOf("unknown")))

        assertFalse(emptyPerms.isWriteAllowed(setOf("initiator")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("approver")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("unknown")))
    }

    @Test
    fun testWithAnyEveryone() {

        setRoles(listOf())
        setStatuses(listOf())
        setStatusForRecord("")

        val permsDef0 = PermissionsDef.create {
            withMatrix(
                mapOf(
                    Pair(
                        "EVERYONE",
                        mapOf(
                            Pair("ANY", PermissionLevel.WRITE)
                        )
                    )
                )
            )
        }

        val perms0 = getPerms(permsDef0)
        assertThat(perms0.isWriteAllowed(listOf("EVERYONE"))).isTrue
        assertThat(perms0.isReadAllowed(listOf("EVERYONE"))).isTrue

        val permsDef1 = PermissionsDef.create {
            withMatrix(
                mapOf(
                    Pair(
                        "EVERYONE",
                        mapOf(
                            Pair("ANY", PermissionLevel.READ)
                        )
                    )
                )
            )
        }

        val perms1 = getPerms(permsDef1)
        assertThat(perms1.isWriteAllowed(listOf("EVERYONE"))).isFalse
        assertThat(perms1.isReadAllowed(listOf("EVERYONE"))).isTrue

        val permsDef2 = PermissionsDef.create {}

        val perms2 = getPerms(permsDef2)
        assertThat(perms2.isWriteAllowed(listOf("EVERYONE"))).isFalse
        assertThat(perms2.isReadAllowed(listOf("EVERYONE"))).isFalse
    }

    @Test
    fun testWithEmptyStatusInConfig() {

        setRoles(listOf("initiator", "approver"))
        setStatuses(listOf("EMPTY", "draft", "approve", "scanning"))

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

        setStatusForRecord("")
        val emptyPerms = getPerms(permsDef)

        assertFalse(emptyPerms.isReadAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isReadAllowed(setOf("approver")))
        assertFalse(emptyPerms.isReadAllowed(setOf("unknown")))

        assertFalse(emptyPerms.isWriteAllowed(setOf("initiator")))
        assertTrue(emptyPerms.isWriteAllowed(setOf("approver")))
        assertFalse(emptyPerms.isWriteAllowed(setOf("unknown")))
    }
}
