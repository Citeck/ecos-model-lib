package ru.citeck.ecos.model.lib.permissions

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.EcosModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.RecordsServiceFactory
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionsMatrixTest {

    @Test
    fun test() {

        val services = EcosModelServiceFactory()
        services.recordsServices = RecordsServiceFactory()
        services.recordsServices.recordsService.register(
            RecordsDaoBuilder.create("test")
                .addRecord(RecordRef.valueOf("in-draft"), TestDto("draft"))
                .addRecord(RecordRef.valueOf("in-approve"), TestDto("approve"))
                .addRecord(RecordRef.valueOf("in-scanning"), TestDto("scanning"))
                .build())

        val permissionsService = services.permissionsService

        val permsDef = PermissionsDef.create {
            setMatrix(mapOf(
                Pair("initiator", mapOf(
                    Pair("draft", PermissionLevel.WRITE),
                    Pair("approve", PermissionLevel.READ),
                    Pair("scanning", PermissionLevel.NONE)
                )),
                Pair("approver", mapOf(
                    Pair("draft", PermissionLevel.READ),
                    Pair("approve", PermissionLevel.WRITE),
                    Pair("scanning", PermissionLevel.NONE)
                )),
                Pair("scan-man", mapOf(
                    Pair("draft", PermissionLevel.NONE),
                    Pair("approve", PermissionLevel.NONE),
                    Pair("scanning", PermissionLevel.WRITE)
                ))
            ))
        }

        val draftPerms = permissionsService.getPermissions(RecordRef.valueOf("test@in-draft"), permsDef)

        assertTrue(draftPerms.isReadAllowed(setOf("initiator")))
        assertTrue(draftPerms.isWriteAllowed(setOf("initiator")))
        assertTrue(draftPerms.isAllowed(setOf("initiator"), PermissionType.READ))
        assertTrue(draftPerms.isAllowed(setOf("initiator"), PermissionType.WRITE))
        assertTrue(draftPerms.isAllowed(setOf("initiator"), "READ"))
        assertTrue(draftPerms.isAllowed(setOf("initiator"), "WRITE"))

        assertTrue(draftPerms.isAllowed(setOf("approver"), PermissionType.READ))
        assertFalse(draftPerms.isAllowed(setOf("approver"), PermissionType.WRITE))

        assertFalse(draftPerms.isAllowed(setOf("unknown"), PermissionType.READ))
        assertFalse(draftPerms.isAllowed(setOf("unknown"), PermissionType.WRITE))

        assertFalse(draftPerms.isAllowed(setOf("scan-man"), PermissionType.READ))
        assertFalse(draftPerms.isAllowed(setOf("scan-man"), PermissionType.WRITE))

        val approvePerms = permissionsService.getPermissions(RecordRef.valueOf("test@in-approve"), permsDef)

        assertTrue(approvePerms.isReadAllowed(setOf("initiator")))
        assertFalse(approvePerms.isWriteAllowed(setOf("initiator")))
        assertTrue(approvePerms.isAllowed(setOf("initiator"), PermissionType.READ))
        assertFalse(approvePerms.isAllowed(setOf("initiator"), PermissionType.WRITE))
        assertTrue(approvePerms.isAllowed(setOf("initiator"), "READ"))
        assertFalse(approvePerms.isAllowed(setOf("initiator"), "WRITE"))

        assertTrue(approvePerms.isAllowed(setOf("approver"), PermissionType.READ))
        assertTrue(approvePerms.isAllowed(setOf("approver"), PermissionType.WRITE))

        assertFalse(approvePerms.isAllowed(setOf("unknown"), PermissionType.READ))
        assertFalse(approvePerms.isAllowed(setOf("unknown"), PermissionType.WRITE))

        assertFalse(approvePerms.isAllowed(setOf("scan-man"), PermissionType.READ))
        assertFalse(approvePerms.isAllowed(setOf("scan-man"), PermissionType.WRITE))

        val scanPerms = permissionsService.getPermissions(RecordRef.valueOf("test@in-scanning"), permsDef)

        assertFalse(scanPerms.isReadAllowed(setOf("initiator")))
        assertFalse(scanPerms.isWriteAllowed(setOf("initiator")))
        assertFalse(scanPerms.isAllowed(setOf("initiator"), PermissionType.READ))
        assertFalse(scanPerms.isAllowed(setOf("initiator"), PermissionType.WRITE))
        assertFalse(scanPerms.isAllowed(setOf("initiator"), "READ"))
        assertFalse(scanPerms.isAllowed(setOf("initiator"), "WRITE"))

        assertFalse(scanPerms.isAllowed(setOf("approver"), PermissionType.READ))
        assertFalse(scanPerms.isAllowed(setOf("approver"), PermissionType.WRITE))

        assertFalse(scanPerms.isAllowed(setOf("unknown"), PermissionType.READ))
        assertFalse(scanPerms.isAllowed(setOf("unknown"), PermissionType.WRITE))

        assertTrue(scanPerms.isAllowed(setOf("scan-man"), PermissionType.READ))
        assertTrue(scanPerms.isAllowed(setOf("scan-man"), PermissionType.WRITE))
    }

    class TestDto(val _status: String)
}
