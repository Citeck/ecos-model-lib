package ru.citeck.ecos.model.lib.comments.dto

import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.webapp.api.entity.EntityRef

data class CommentDto(
    val text: String,
    val record: EntityRef,
    val tags: List<CommentTag> = emptyList()
)

data class CommentTag(
    val type: CommentTagType,
    val name: MLText
)

enum class CommentTagType {
    TASK,
    ACTION,
    INTEGRATION
}
