package ru.citeck.ecos.model.lib.attributes.dto

import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json

data class AttributeDef(
    val id: String,
    val name: MLText,
    val type: AttributeType,
    val config: ObjectData,
    val multiple: Boolean,
    val mandatory: Boolean,
    val constraint: AttConstraintDef
) {

    companion object {

        fun create() : Builder {
            return Builder()
        }

        fun create(builder: Builder.() -> Unit) : AttributeDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy() : Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit) : AttributeDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        private var id: String? = null
        private var name: MLText? = null
        private var type: AttributeType = AttributeType.TEXT
        private var config: ObjectData = ObjectData.create()
        private var multiple: Boolean = false
        private var mandatory: Boolean = false
        private var constraint: AttConstraintDef = AttConstraintDef.NONE

        constructor(base: AttributeDef) : this() {
            id = base.id
            name = MLText.copy(base.name)
            type = base.type
            config = ObjectData.deepCopy(base.config)!!
            multiple = base.multiple
            mandatory = base.mandatory
            constraint = Json.mapper.copy(base.constraint)!!
        }

        fun setId(id: String) : Builder {
            this.id = id
            return this
        }

        fun setName(name: MLText) : Builder {
            this.name = name
            return this
        }

        fun setType(type: AttributeType) : Builder {
            this.type = type
            return this
        }

        fun setConfig(config: ObjectData) : Builder {
            this.config = config
            return this
        }

        fun setMultiple(multiple: Boolean) : Builder {
            this.multiple = multiple
            return this
        }

        fun setMandatory(mandatory: Boolean) : Builder {
            this.mandatory = mandatory
            return this
        }

        fun setConstraint(constraint: AttConstraintDef) : Builder {
            this.constraint = constraint
            return this
        }

        fun build() : AttributeDef {
            return AttributeDef(id!!, name!!, type, config, multiple, mandatory, constraint)
        }
    }
}
