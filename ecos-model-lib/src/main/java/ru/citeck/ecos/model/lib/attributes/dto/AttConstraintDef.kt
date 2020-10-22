package ru.citeck.ecos.model.lib.attributes.dto

import ru.citeck.ecos.commons.data.ObjectData

class AttConstraintDef(
    val type: String,
    val config: ObjectData
) {
    companion object {
        val NONE = AttConstraintDef("", ObjectData.create())
    }
}
