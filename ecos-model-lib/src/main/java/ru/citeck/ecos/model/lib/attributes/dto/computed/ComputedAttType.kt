package ru.citeck.ecos.model.lib.attributes.dto.computed

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttType

enum class ComputedAttType {

    SCRIPT,
    ATTRIBUTE,
    VALUE,
    TEMPLATE,
    COUNTER,
    @JsonEnumDefaultValue
    NONE;

    fun toRecordComputedType(): RecordComputedAttType {
        return when (this) {
            SCRIPT -> RecordComputedAttType.SCRIPT
            ATTRIBUTE -> RecordComputedAttType.ATTRIBUTE
            VALUE -> RecordComputedAttType.VALUE
            TEMPLATE -> RecordComputedAttType.TEMPLATE
            COUNTER -> RecordComputedAttType.NONE
            NONE -> RecordComputedAttType.NONE
        }
    }
}
