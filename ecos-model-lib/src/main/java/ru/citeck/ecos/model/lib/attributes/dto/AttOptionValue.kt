package ru.citeck.ecos.model.lib.attributes.dto

import com.fasterxml.jackson.annotation.JsonValue
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.records3.record.atts.value.AttValue

data class AttOptionValue(
    val label: MLText,
    val value: String
) : AttValue {

    companion object {
        private const val ATT_LABEL = "label"
        private const val ATT_VALUE = "value"
    }

    override fun asText(): String {
        return value
    }

    override fun getDisplayName(): Any {
        return label
    }

    override fun getAtt(name: String): Any? {
        return when (name) {
            ATT_LABEL -> label
            ATT_VALUE -> value
            else -> null
        }
    }

    @JsonValue
    override fun asJson(): Any {
        return DataValue.createObj()
            .set(ATT_LABEL, label)
            .set(ATT_VALUE, value)
    }
}
