package ru.citeck.ecos.model.lib.attributes.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttDef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@IncludeNonDefault
@JsonDeserialize(builder = AttributeDef.Builder::class)
@JackJsonDeserialize(builder = AttributeDef.Builder::class)
data class AttributeDef(
    val id: String,
    val name: MLText,
    val type: AttributeType,
    val config: ObjectData,
    val multiple: Boolean,
    val mandatory: Boolean,
    val computed: ComputedAttDef,
    val constraint: AttConstraintDef
) {

    companion object {

        fun create(): Builder {
            return Builder()
        }

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

    class Builder() {

        var id: String = ""
        var name: MLText = MLText.EMPTY
        var type: AttributeType = AttributeType.TEXT
        var config: ObjectData = ObjectData.create()
        var multiple: Boolean = false
        var mandatory: Boolean = false
        var computed: ComputedAttDef = ComputedAttDef.EMPTY
        var constraint: AttConstraintDef = AttConstraintDef.EMPTY

        constructor(base: AttributeDef) : this() {
            id = base.id
            name = base.name
            type = base.type
            config = ObjectData.deepCopyOrNew(base.config)
            multiple = base.multiple
            mandatory = base.mandatory
            computed = base.computed
            constraint = base.constraint
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText): Builder {
            this.name = name
            return this
        }

        fun withType(type: AttributeType): Builder {
            this.type = type
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

        fun build(): AttributeDef {
            return AttributeDef(id, name, type, config, multiple, mandatory, computed, constraint)
        }
    }
}
