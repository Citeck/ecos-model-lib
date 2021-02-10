package ru.citeck.ecos.model.lib.attributes.dto

import ru.citeck.ecos.commons.data.ObjectData

data class AttConstraintDef(
    val type: String,
    val config: ObjectData
) {
    companion object {
        val EMPTY = AttConstraintDef("", ObjectData.create())
    }
}
