package ru.citeck.ecos.model.lib.type.dto

import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData

data class AttributeDef(
    val id: String,
    val name: MLText,
    val type: AttributeType,
    val config: ObjectData,
    val multiple: Boolean,
    val mandatory: Boolean
)
