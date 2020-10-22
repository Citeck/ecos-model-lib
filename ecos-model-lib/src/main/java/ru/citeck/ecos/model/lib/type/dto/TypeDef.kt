package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.records2.RecordRef
import java.util.*

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
data class TypeDef(

    val id: String,
    val name: MLText,
    val description: MLText,
    val sourceId: String,
    val parentRef: RecordRef = RecordRef.EMPTY,
    val formRef: RecordRef = RecordRef.EMPTY,
    val journalRef: RecordRef = RecordRef.EMPTY,
    val system: Boolean,
    val dashboardType: String,
    val inheritActions: Boolean,
    val inheritForm: Boolean,

    val dispNameTemplate: MLText,

    val numTemplateRef: RecordRef = RecordRef.EMPTY,
    val inheritNumTemplate: Boolean,

    val aliases: List<String> = ArrayList(),

    val actions: List<RecordRef> = ArrayList(),

    val configFormRef: RecordRef = RecordRef.EMPTY,
    val config: ObjectData = ObjectData.create(),

    val createVariants: List<CreateVariant> = ArrayList<CreateVariant>(),

    val attributes: ObjectData = ObjectData.create()
)
