package ru.citeck.ecos.model.lib.attributes.dto

import ecos.com.fasterxml.jackson210.annotation.JsonEnumDefaultValue

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

    OPTIONS
}
