package ru.citeck.ecos.model.lib.type.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.webapp.api.entity.EntityRef

@IncludeNonDefault
@JsonDeserialize(builder = TypeContentConfig.Builder::class)
data class TypeContentConfig(
    val path: String,
    val previewPath: String,
    val storageRef: EntityRef,
    val storageConfig: ObjectData
) {

    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeContentConfig {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeContentConfig {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var path: String = ""
        var previewPath: String = ""
        var storageRef: EntityRef = EntityRef.EMPTY
        var storageConfig: ObjectData = ObjectData.create()

        constructor(base: TypeContentConfig) : this() {
            this.path = base.path
            this.previewPath = base.previewPath
            this.storageRef = base.storageRef
            this.storageConfig = base.storageConfig.deepCopy()
        }

        fun withPath(path: String?): Builder {
            this.path = path ?: ""
            return this
        }

        fun withPreviewPath(previewPath: String?): Builder {
            this.previewPath = previewPath ?: ""
            return this
        }

        fun withStorageRef(storageRef: EntityRef?): Builder {
            this.storageRef = storageRef ?: EntityRef.EMPTY
            return this
        }

        fun withStorageConfig(storageConfig: ObjectData?): Builder {
            this.storageConfig = storageConfig ?: ObjectData.create()
            return this
        }

        fun build(): TypeContentConfig {
            return TypeContentConfig(
                path,
                previewPath,
                storageRef,
                storageConfig
            )
        }
    }
}
