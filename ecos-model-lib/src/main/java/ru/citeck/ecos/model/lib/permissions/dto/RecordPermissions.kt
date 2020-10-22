package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.records2.RecordRef

data class RecordPermissions(
    val id: String,
    val typeRef: RecordRef = RecordRef.EMPTY,
    val permissions: Permissions = Permissions(),
    val attributes: Map<String, Permissions> = emptyMap()
)
