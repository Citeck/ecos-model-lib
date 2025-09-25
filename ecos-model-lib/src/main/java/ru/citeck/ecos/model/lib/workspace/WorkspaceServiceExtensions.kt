@file:Suppress("unused")

package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.model.lib.workspace.IdInWs.Companion.create

fun WorkspaceService?.convertToIdInWsSafe(strId: String): IdInWs {
    return this?.convertToIdInWs(strId) ?: create("", strId)
}

fun WorkspaceService?.convertToStrIdSafe(idInWs: IdInWs): String {
    if (idInWs.workspace.isEmpty() || idInWs.id.isEmpty()) {
        return idInWs.id
    }
    if (this == null) {
        error("WorkspaceService is null. Id can't be converted: '$idInWs'")
    }
    return this.convertToStrId(idInWs)
}
