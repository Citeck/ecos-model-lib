package ru.citeck.ecos.model.lib.attributes.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef

@IncludeNonDefault
@JsonDeserialize(builder = AttributeDef.Builder::class)
data class AttributeDef(
    val id: String,
    val name: MLText,
    val type: AttributeType,
    val config: ObjectData,
    val multiple: Boolean,
    val mandatory: Boolean,
    val computed: ComputedAttDef,
    val constraint: AttConstraintDef,
    val index: AttIndexDef
) {

    companion object {

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): AttributeDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): AttributeDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    fun withPrefixedId(prefix: String): AttributeDef {
        return copy().withId("$prefix:$id").build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText.EMPTY
        var type: AttributeType = AttributeType.TEXT
        var config: ObjectData = ObjectData.create()
        var multiple: Boolean = false
        var mandatory: Boolean = false
        var computed: ComputedAttDef = ComputedAttDef.EMPTY
        var constraint: AttConstraintDef = AttConstraintDef.EMPTY
        var index: AttIndexDef = AttIndexDef.EMPTY

        constructor(base: AttributeDef) : this() {
            id = base.id
            name = base.name
            type = base.type
            config = ObjectData.deepCopyOrNew(base.config)
            multiple = base.multiple
            mandatory = base.mandatory
            computed = base.computed
            constraint = base.constraint
            index = base.index
        }

        fun withId(id: String?): Builder {
            this.id = id ?: ""
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText.EMPTY
            return this
        }

        fun withType(type: AttributeType?): Builder {
            this.type = type ?: AttributeType.TEXT
            return this
        }

        fun withComputed(computed: ComputedAttDef?): Builder {
            this.computed = computed ?: ComputedAttDef.EMPTY
            return this
        }

        fun withConfig(config: ObjectData?): Builder {
            this.config = config ?: ObjectData.create()
            return this
        }

        fun withMultiple(multiple: Boolean?): Builder {
            this.multiple = multiple ?: false
            return this
        }

        fun withMandatory(mandatory: Boolean?): Builder {
            this.mandatory = mandatory ?: false
            return this
        }

        fun withConstraint(constraint: AttConstraintDef?): Builder {
            this.constraint = constraint ?: AttConstraintDef.EMPTY
            return this
        }

        fun withIndex(index: AttIndexDef?): Builder {
            this.index = index ?: AttIndexDef.EMPTY
            return this
        }

        fun build(): AttributeDef {
            return AttributeDef(
                id = id,
                name = name,
                type = type,
                config = config,
                multiple = multiple,
                mandatory = mandatory,
                computed = computed,
                constraint = constraint,
                index = index
            )
        }
    }
}
