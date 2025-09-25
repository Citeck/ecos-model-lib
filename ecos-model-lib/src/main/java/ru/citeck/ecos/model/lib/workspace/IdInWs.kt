package ru.citeck.ecos.model.lib.workspace

import com.fasterxml.jackson.annotation.JsonIgnore

class IdInWs private constructor(
    val workspace: String,
    val id: String
) {
    companion object {

        const val WS_DELIM = ":"
        val EMPTY = create("", "")

        @JvmStatic
        fun create(id: String): IdInWs {
            return create("", id)
        }

        @JvmStatic
        fun create(workspace: String, id: String): IdInWs {
            return IdInWs(workspace.trim(), id.trim())
        }
    }

    @JsonIgnore
    fun isEmpty() = id.isEmpty()

    override fun toString(): String {
        if (workspace.isEmpty()) {
            return id
        }
        // double delim to separate two forms of text representation:
        // 1. workspaceId::localId
        // 2. workspaceSystemId:localId
        return "$workspace$WS_DELIM$WS_DELIM$id"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as IdInWs
        return workspace == other.workspace && id == other.id
    }

    override fun hashCode(): Int {
        return workspace.hashCode() * 31 + id.hashCode()
    }
}
