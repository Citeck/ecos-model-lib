package ru.citeck.ecos.model.lib.attributes.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

/**
 * Warning: Fixed values order! New values should be added after last element.
 */
enum class AttributeType {

    ASSOC,

    PERSON,
    AUTHORITY_GROUP,
    AUTHORITY,

    @JsonEnumDefaultValue
    TEXT,
    MLTEXT,

    NUMBER,
    BOOLEAN,

    DATE,
    DATETIME,

    CONTENT,

    JSON,
    BINARY,

    OPTIONS,

    ENTITY_REF;

    companion object {

        /**
         * Check if the attribute type represents a reference to another entity.
         * Returns true for ASSOC, PERSON, AUTHORITY_GROUP, AUTHORITY and ENTITY_REF.
         */
        @JvmStatic
        fun isAssocLike(type: AttributeType?): Boolean {
            return when (type) {
                ASSOC,
                PERSON,
                AUTHORITY_GROUP,
                AUTHORITY,
                ENTITY_REF -> true
                else -> false
            }
        }
    }
}
