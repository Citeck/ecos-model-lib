package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.records2.RecordRef

data class RecordPermsDef(
    val id: String,
    val typeRef: RecordRef,
    val permissions: PermissionsDef,
    val attributes: Map<String, PermissionsDef>
) {

    companion object {

        @JvmStatic
        fun create() : Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit) : RecordPermsDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy() : Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit) : RecordPermsDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        lateinit var id: String
            private set
        var typeRef: RecordRef = RecordRef.EMPTY
            private set
        var permissions: PermissionsDef = PermissionsDef.EMPTY
            private set
        var attributes: Map<String, PermissionsDef> = emptyMap()
            private set

        constructor(base: RecordPermsDef) : this() {
            this.id = base.id
            this.typeRef = base.typeRef
            this.permissions = base.permissions
            this.attributes = HashMap(base.attributes)
        }

        fun setId(id: String) : Builder {
            this.id = id
            return this
        }

        fun setTypeRef(typeRef: RecordRef) : Builder {
            this.typeRef = typeRef
            return this
        }

        fun setPermissions(permissions: PermissionsDef) : Builder {
            this.permissions = permissions
            return this
        }

        fun setAttributes(attributes: Map<String, PermissionsDef>) : Builder {
            this.attributes = attributes
            return this
        }

        fun build() : RecordPermsDef {
            return RecordPermsDef(id, typeRef, permissions, attributes)
        }
    }
}