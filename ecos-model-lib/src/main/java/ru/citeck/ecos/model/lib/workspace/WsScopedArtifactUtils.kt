package ru.citeck.ecos.model.lib.workspace

object WsScopedArtifactUtils {

    const val SCOPED_ID_PREFIX_PREFIX = "ws_"
    const val SCOPED_ID_PREFIX_DELIM = "/"

    @JvmStatic
    fun removeWsPrefixFromId(id: String): String {
        if (!id.startsWith(SCOPED_ID_PREFIX_PREFIX)) {
            return id
        }
        return id.substringAfterLast(SCOPED_ID_PREFIX_DELIM)
    }

    @JvmStatic
    fun addWsPrefixToId(localId: String, wsSysId: String): String {
        if (wsSysId.isBlank()) {
            return localId
        }
        return SCOPED_ID_PREFIX_PREFIX + wsSysId + SCOPED_ID_PREFIX_DELIM + localId
    }
}
