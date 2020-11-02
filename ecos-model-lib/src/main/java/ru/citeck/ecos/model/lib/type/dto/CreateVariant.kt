package ru.citeck.ecos.model.lib.type.dto

import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.records2.RecordRef

data class CreateVariant(
    val id: String,
    val name: MLText,
    val formRef: RecordRef,
    val recordRef: RecordRef,
    val attributes: ObjectData = ObjectData.create()
)
