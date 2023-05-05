package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionRule
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.webapp.api.perms.PermissionType
import kotlin.test.assertEquals

class PermsDefTest {

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
            withRules(
                listOf(
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("AddChildren")
                    ),
                    PermissionRule(
                        roles = setOf("unknown-status-reader"),
                        permissions = setOf(PermissionType.READ.name)
                    )
                )
            )
        }

        val recordsServices = RecordsServiceFactory()
        val records = recordsServices.recordsServiceV1
        val defFromRecords = records.getAtts(permsDef, PermissionsDef::class.java)
        assertEquals(permsDef, defFromRecords)

        val read = recordsServices.dtoSchemaReader.read(TypePermsDef::class.java)
        println(read)
    }
}
