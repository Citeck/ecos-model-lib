package ru.citeck.ecos.model.lib.delegation.dto

import ecos.com.fasterxml.jackson210.annotation.JsonIgnore
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType
import com.fasterxml.jackson.annotation.JsonIgnore as JackJsonIgnore

data class PermissionDelegateData(
    val permissionType: PermissionType = PermissionType.READ,
    val from: String = "",
    val to: List<String> = emptyList()
) {

    @JsonIgnore
    @JackJsonIgnore
    fun isEmpty(): Boolean {
        return from.isBlank() || to.isEmpty()
    }

    @JsonIgnore
    @JackJsonIgnore
    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }
}
