package ru.citeck.ecos.model.lib.delegation.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

data class PermissionDelegateData(
    val permissionType: PermissionType = PermissionType.READ,
    val from: String = "",
    val to: List<String> = emptyList()
) {

    @JsonIgnore
    fun isEmpty(): Boolean {
        return from.isBlank() || to.isEmpty()
    }

    @JsonIgnore
    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }
}
