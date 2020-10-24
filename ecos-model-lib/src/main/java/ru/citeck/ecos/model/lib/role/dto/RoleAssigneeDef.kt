package ru.citeck.ecos.model.lib.role.dto

data class RoleAssigneeDef(
    val attribute: String = "",
    val authorities: List<String> = emptyList()
) {
    companion object {
        val EMPTY = RoleAssigneeDef()
    }
}
