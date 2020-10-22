package ru.citeck.ecos.model.lib.status.dto

import ru.citeck.ecos.commons.data.MLText

data class StatusDef(
    val id: String,
    val name: MLText,
    val theme: String
)
