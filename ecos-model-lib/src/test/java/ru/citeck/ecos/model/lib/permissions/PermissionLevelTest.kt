package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import kotlin.test.assertEquals

class PermissionLevelTest {

    @Test
    fun unionTest() {

        assertEquals(PermissionLevel.NONE, PermissionLevel.NONE.union(PermissionLevel.NONE))
        assertEquals(PermissionLevel.NONE, PermissionLevel.NONE.union(null))

        assertEquals(PermissionLevel.READ, PermissionLevel.NONE.union(PermissionLevel.READ))
        assertEquals(PermissionLevel.READ, PermissionLevel.READ.union(PermissionLevel.READ))

        assertEquals(PermissionLevel.READ, PermissionLevel.READ.union(PermissionLevel.NONE))
        assertEquals(PermissionLevel.READ, PermissionLevel.READ.union(null))

        assertEquals(PermissionLevel.WRITE, PermissionLevel.NONE.union(PermissionLevel.WRITE))
        assertEquals(PermissionLevel.WRITE, PermissionLevel.READ.union(PermissionLevel.WRITE))
        assertEquals(PermissionLevel.WRITE, PermissionLevel.WRITE.union(PermissionLevel.WRITE))

        assertEquals(PermissionLevel.WRITE, PermissionLevel.WRITE.union(PermissionLevel.NONE))
        assertEquals(PermissionLevel.WRITE, PermissionLevel.WRITE.union(PermissionLevel.READ))
        assertEquals(PermissionLevel.WRITE, PermissionLevel.WRITE.union(null))
    }
}
