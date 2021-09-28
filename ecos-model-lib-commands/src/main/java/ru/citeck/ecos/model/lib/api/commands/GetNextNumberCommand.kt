package ru.citeck.ecos.model.lib.api.commands

import ru.citeck.ecos.commands.annotation.CommandType
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.records2.RecordRef

@CommandType("ecos.number.template.get-next")
data class GetNextNumberCommand(
    val templateRef: RecordRef,
    val model: ObjectData
)

data class GetNextNumberResult(
    val number: Long
)
