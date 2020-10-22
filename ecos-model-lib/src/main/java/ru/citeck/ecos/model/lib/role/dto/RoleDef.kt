package ru.citeck.ecos.model.lib.role.dto

import ru.citeck.ecos.commons.data.MLText

data class RoleDef(
    val id: String,
    val name: MLText,
    val type: RoleType,
    val attribute: String = "",
    val authorities: List<String> = emptyList()
)
