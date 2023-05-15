package ru.citeck.ecos.model.lib.comments.service

import mu.KotlinLogging
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.comments.COMMENT_ATT_RECORD
import ru.citeck.ecos.model.lib.comments.COMMENT_ATT_TAGS
import ru.citeck.ecos.model.lib.comments.COMMENT_ATT_TEXT
import ru.citeck.ecos.model.lib.comments.dto.CommentDto
import ru.citeck.ecos.records3.record.atts.dto.RecordAtts
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.entity.EntityRef

class DefaultCommentsService(services: ModelServiceFactory) : CommentsService {

    private val recordsService = services.records.recordsServiceV1

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun createComment(comment: CommentDto): EntityRef {
        log.debug { "Create comment: $comment" }

        val recordAtt = RecordAtts("${AppName.EMODEL}/comment@")
        recordAtt.setAtt(COMMENT_ATT_TEXT, comment.text)
        recordAtt.setAtt(COMMENT_ATT_RECORD, comment.record)

        if (comment.tags.isNotEmpty()) {
            recordAtt.setAtt(COMMENT_ATT_TAGS, comment.tags)
        }

        val createdCommentRef = recordsService.mutate(recordAtt)

        log.debug { "Comment created: $createdCommentRef" }

        return createdCommentRef
    }
}
