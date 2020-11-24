package ru.citeck.ecos.model.lib.attributes.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttDef

@JsonDeserialize(builder = AttributeDef.Builder::class)
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
        var name: MLText = MLText()
        var type: AttributeType = AttributeType.TEXT
        var config: ObjectData = ObjectData.create()
        var multiple: Boolean = false
        var mandatory: Boolean = false
        var computed: ComputedAttDef = ComputedAttDef.EMPTY
        var constraint: AttConstraintDef = AttConstraintDef.NONE

        constructor(base: AttributeDef) : this() {
            id = base.id
            name = MLText.copy(base.name)!!
            type = base.type
            config = ObjectData.deepCopy(base.config)!!
            multiple = base.multiple
            mandatory = base.mandatory
            computed = base.computed
            constraint = Json.mapper.copy(base.constraint)!!
        }

        fun withComputed(computed: ComputedAttDef): Builder {
            this.computed = computed
            return this
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

        fun withConfig(config: ObjectData): Builder {
            this.config = config
            return this
        }

        fun withMultiple(multiple: Boolean): Builder {
            this.multiple = multiple
            return this
        }

        fun withMandatory(mandatory: Boolean): Builder {
            this.mandatory = mandatory
            return this
        }

        fun withConstraint(constraint: AttConstraintDef): Builder {
            this.constraint = constraint
            return this
        }

        fun build(): AttributeDef {
            return AttributeDef(id, name, type, config, multiple, mandatory, computed, constraint)
        }
    }
}
