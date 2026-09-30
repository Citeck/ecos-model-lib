@file:Suppress("unused")

package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.model.lib.workspace.IdInWs.Companion.create
import ru.citeck.ecos.webapp.api.entity.EntityRef

/**
 * Rebind a single [ref] to [workspace] when an artifact is deployed:
 * - `CURRENT_WS:` placeholder → target ws prefix (stripped to bare for a global deploy);
 * - already ws-prefixed → unchanged;
 * - unprefixed but present in [coDeployedRefs] (artifacts landing in the same workspace
 *   alongside this one) → promoted with the target ws prefix; an auto artifact of a type
 *   (`uiserv/journal@type$my-type`) is promoted when its type (`emodel/type@my-type`) is co-deployed;
 * - otherwise unchanged (stays global / unprefixed).
 *
 * [coDeployedRefs] are app-qualified primary refs (e.g. `emodel/type@order-pass`). The membership
 * check only matters in the unprefixed branch, where [ref] has no ws prefix and is directly
 * comparable. Mirrors the workspace-aware deploy logic used by every WsAware artifact handler.
 */
fun WorkspaceService.bindRefToWorkspace(
    ref: EntityRef,
    workspace: String,
    coDeployedRefs: Set<EntityRef> = emptySet()
): EntityRef {
    if (EntityRef.isEmpty(ref)) {
        return ref
    }
    val localId = ref.getLocalId()
    val rebound = replaceCurrentWsPlaceholderToWsPrefix(localId, workspace)
    val newLocalId = when {
        rebound != localId -> rebound
        localId.contains(IdInWs.WS_DELIM) -> localId
        ref in coDeployedRefs -> addWsPrefixToId(localId, workspace)
        isAutoArtifactOfCoDeployedType(localId, coDeployedRefs) -> {
            val typeLocalId = localId.substring(TypeConstants.AUTO_ARTIFACT_ID_PREFIX.length)
            TypeConstants.AUTO_ARTIFACT_ID_PREFIX + addWsPrefixToId(typeLocalId, workspace)
        }
        else -> localId
    }
    return if (newLocalId == localId) {
        ref
    } else {
        ref.withLocalId(newLocalId)
    }
}

private fun isAutoArtifactOfCoDeployedType(localId: String, coDeployedRefs: Set<EntityRef>): Boolean {
    if (!localId.startsWith(TypeConstants.AUTO_ARTIFACT_ID_PREFIX)) {
        return false
    }
    val typeLocalId = localId.substring(TypeConstants.AUTO_ARTIFACT_ID_PREFIX.length)
    return EntityRef.create(TypeConstants.TYPE_APP, TypeConstants.TYPE_SOURCE, typeLocalId) in coDeployedRefs
}

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
