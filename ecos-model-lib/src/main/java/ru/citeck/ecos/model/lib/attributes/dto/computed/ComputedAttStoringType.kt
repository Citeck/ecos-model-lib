package ru.citeck.ecos.model.lib.attributes.dto.computed

enum class ComputedAttStoringType {
    NONE,
    /**
     * Evaluate and store attribute
     */
    ON_EMPTY,
    ON_CREATE,
    ON_MUTATE
}
