package ru.citeck.ecos.model.lib.attributes.dto

import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText

data class AttOptionDef(
    val label: MLText,
    val value: DataValue
)
