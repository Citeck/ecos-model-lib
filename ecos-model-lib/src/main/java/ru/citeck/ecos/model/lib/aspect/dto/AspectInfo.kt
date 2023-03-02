package ru.citeck.ecos.model.lib.aspect.dto

import ecos.com.fasterxml.jackson210.annotation.JsonIgnore
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.utils.ModelUtils
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = AspectInfo.Builder::class)
@JackJsonDeserialize(builder = AspectInfo.Builder::class)
@IncludeNonDefault
class AspectInfo(
    val id: String,
    val prefix: String,
    val attributes: List<AttributeDef>,
    val systemAttributes: List<AttributeDef>
) {

    companion object {

        @JvmField
        val EMPTY = create().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): AspectInfo {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): AspectInfo {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    @JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    fun getAllAttributes(): List<AttributeDef> {
        val result = ArrayList<AttributeDef>()
        result.addAll(attributes)
        result.addAll(systemAttributes)
        return result
    }

    @JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    fun isEmpty(): Boolean {
        return attributes.isEmpty() && systemAttributes.isEmpty()
    }

    class Builder() {

        var id: String = ""
        var prefix: String = ""

        var attributes: List<AttributeDef> = emptyList()
        var systemAttributes: List<AttributeDef> = emptyList()

        constructor(base: AspectInfo) : this() {
            this.id = base.id
            this.prefix = base.prefix
            this.attributes = DataValue.create(base.attributes).asList(AttributeDef::class.java)
            this.systemAttributes = DataValue.create(base.systemAttributes).asList(AttributeDef::class.java)
        }

        fun withId(id: String?): Builder {
            this.id = id ?: ""
            return this
        }

        fun withPrefix(prefix: String?): Builder {
            this.prefix = prefix ?: ""
            return this
        }

        fun withAttributes(attributes: List<AttributeDef>?): Builder {
            this.attributes = attributes?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun withSystemAttributes(systemAttributes: List<AttributeDef>?): Builder {
            this.systemAttributes = systemAttributes?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun build(): AspectInfo {

            val (attributes, systemAttributes) = ModelUtils.getMergedModelAtts(
                attributes,
                systemAttributes
            )

            return AspectInfo(id, prefix, attributes, systemAttributes)
        }
    }
}
