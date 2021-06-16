package ru.citeck.ecos.model.lib.attributes.dto

/**
 * Warning: Fixed values order! New values should be added after last element.
 */
enum class AttributeType {

    ASSOC,

    PERSON,
    AUTHORITY_GROUP,
    AUTHORITY,

    TEXT,
    MLTEXT,

    NUMBER,
    BOOLEAN,

    DATE,
    DATETIME,

    CONTENT,

    JSON
}
