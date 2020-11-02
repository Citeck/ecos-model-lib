package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ru.citeck.ecos.records2.RecordRef

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
data class TypeDef(

    val id: String,
    val parentRef: RecordRef? = null,
    val model: TypeModelDef = TypeModelDef.EMPTY
)
