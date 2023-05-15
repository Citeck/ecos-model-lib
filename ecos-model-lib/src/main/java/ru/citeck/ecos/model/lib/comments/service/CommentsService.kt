package ru.citeck.ecos.model.lib.comments.service

import ru.citeck.ecos.model.lib.comments.dto.CommentDto
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface CommentsService {

    fun createComment(comment: CommentDto): EntityRef
}
