package ru.citeck.ecos.model.lib.attributes.dto.computed

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class ComputedAttStoringType {

    @JsonEnumDefaultValue
    NONE,

    /**
     * Evaluate and store attribute
     */
    ON_EMPTY,
    ON_CREATE,
    ON_MUTATE
}
