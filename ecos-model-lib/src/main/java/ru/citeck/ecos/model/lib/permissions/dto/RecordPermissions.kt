package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.records2.RecordRef

data class RecordPermissions(
    val id: String,
    val typeRef: RecordRef,
    val permissions: PermissionsDef,
    val attributes: Map<String, PermissionsDef>
) {

    companion object {

        fun create() : Builder {
            return Builder()
        }

        fun create(builder: Builder.() -> Unit) : RecordPermissions {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy() : Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit) : RecordPermissions {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        private lateinit var id: String
        private var typeRef: RecordRef = RecordRef.EMPTY

        private var permissions: PermissionsDef = PermissionsDef.EMPTY
        private var attributes: Map<String, PermissionsDef> = emptyMap()

        constructor(base: RecordPermissions) : this() {
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

        fun build() : RecordPermissions {
            return RecordPermissions(id, typeRef, permissions, attributes)
        }
    }
}
