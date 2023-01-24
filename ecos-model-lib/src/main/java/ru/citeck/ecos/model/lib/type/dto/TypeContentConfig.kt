package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@IncludeNonDefault
@JsonDeserialize(builder = TypeContentConfig.Builder::class)
@JackJsonDeserialize(builder = TypeContentConfig.Builder::class)
data class TypeContentConfig(
    val path: String,
    val previewPath: String,
    val storageType: String
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
        var storageType: String = ""

        constructor(base: TypeContentConfig) : this() {
            this.path = base.path
            this.previewPath = base.previewPath
        }

        fun withPath(path: String?): Builder {
            this.path = path ?: ""
            return this
        }

        fun withPreviewPath(previewPath: String?): Builder {
            this.previewPath = previewPath ?: ""
            return this
        }

        fun withStorageType(storageType: String): Builder {
            this.storageType = storageType
            return this
        }

        fun build(): TypeContentConfig {
            return TypeContentConfig(path, previewPath, storageType)
        }
    }
}
