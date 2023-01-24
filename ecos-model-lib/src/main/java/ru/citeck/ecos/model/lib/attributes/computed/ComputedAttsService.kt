package ru.citeck.ecos.model.lib.attributes.computed

import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttDef
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAtt
import ru.citeck.ecos.records3.record.atts.schema.ScalarType
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.impl.AttValueDelegate
import ru.citeck.ecos.webapp.api.entity.EntityRef

class ComputedAttsService(services: ModelServiceFactory) {

    companion object {
        private const val COUNTER_CONFIG_TEMPLATE_KEY = "numTemplateRef"
    }

    private val recordComputedAttsService = services.records.recordComputedAttsService
    private val typesRepo = services.typesRepo
    private val recordsTemplateService = services.records.recordsTemplateService
    private val recordsService = services.records.recordsServiceV1
    private val ecosNumService = services.ecosNumService
    private val attValuesConverter = services.records.attValuesConverter

    fun computeAttsToStore(value: Any, isNewRecord: Boolean): ObjectData {
        val typeStr = recordsService.getAtt(value, RecordConstants.ATT_TYPE + ScalarType.ID.schema).asText()
        return computeAttsToStore(value, isNewRecord, EntityRef.valueOf(typeStr))
    }

    fun computeAttsToStore(value: Any, isNewRecord: Boolean, typeRef: EntityRef): ObjectData {

        if (typeRef.getLocalId().isEmpty()) {
            return ObjectData.create()
        }

        val typeInfo = typesRepo.getTypeInfo(typeRef) ?: return ObjectData.create()

        val attsToEval = LinkedHashMap<String, ComputedAttDef>()
        if (EntityRef.isNotEmpty(typeInfo.numTemplateRef)) {
            val config = ObjectData.create()
            config[COUNTER_CONFIG_TEMPLATE_KEY] = typeInfo.numTemplateRef
            attsToEval[RecordConstants.ATT_DOC_NUM] = ComputedAttDef.create {
                withType(ComputedAttType.COUNTER)
                withConfig(config)
                withStoringType(ComputedAttStoringType.ON_CREATE)
            }
        }
        val attributes = typeInfo.model.getAllAttributes()
        for (attribute in attributes) {
            if (attribute.computed.storingType == ComputedAttStoringType.NONE ||
                attribute.computed.type == ComputedAttType.NONE
            ) {
                continue
            }
            attsToEval.putIfAbsent(attribute.id, attribute.computed)
        }

        val resultAtts = linkedMapOf<String, Any?>()
        val valueToEval = ValueWithData(attValuesConverter.toAttValue(value)!!, resultAtts)
        if (isNewRecord) {
            for ((attId, computed) in attsToEval) {
                if (computed.type == ComputedAttType.COUNTER) {
                    computeAttToStore(resultAtts, valueToEval, attId, computed, isNewRecord)
                }
            }
        }
        for ((attId, computed) in attsToEval) {
            if (computed.type != ComputedAttType.COUNTER) {
                computeAttToStore(resultAtts, valueToEval, attId, computed, isNewRecord)
            }
        }

        return ObjectData.create(resultAtts)
    }

    fun computeDisplayName(value: Any): MLText {
        val typeRef = recordsService.getAtt(value, "_type?id").asText()
        if (typeRef.isBlank()) {
            return MLText.EMPTY
        }
        return computeDisplayName(value, EntityRef.valueOf(typeRef))
    }

    fun computeDisplayName(value: Any, typeRef: EntityRef): MLText {
        val typeInfo = typesRepo.getTypeInfo(typeRef) ?: return MLText.EMPTY
        return computeDisplayName(value, typeInfo)
    }

    fun computeDisplayName(value: Any, typeInfo: TypeInfo): MLText {

        if (!MLText.isEmpty(typeInfo.dispNameTemplate)) {
            return recordsTemplateService.resolve(typeInfo.dispNameTemplate, value)
        }

        var resName = getMLText(value, "name")
        if (MLText.isEmpty(resName) && !MLText.isEmpty(typeInfo.name)) {
            resName = typeInfo.name
        }
        if (MLText.isEmpty(resName) && typeInfo.id.isNotBlank()) {
            resName = MLText(typeInfo.id)
        }
        return resName
    }

    private fun getMLText(value: Any, attName: String): MLText {
        var result: MLText = MLText.EMPTY
        val valueName = recordsService.getAtt(value, "$attName?raw")
        if (valueName.isObject()) {
            result = valueName.getAs(MLText::class.java) ?: MLText.EMPTY
        } else if (valueName.isTextual()) {
            val strValue = valueName.asText()
            if (strValue.isNotBlank()) {
                result = MLText(strValue)
            }
        }
        return result
    }

    private fun computeAttToStore(
        result: MutableMap<String, Any?>,
        value: Any,
        attId: String,
        computed: ComputedAttDef,
        isNewRecord: Boolean
    ) {

        if (computed.storingType == ComputedAttStoringType.NONE ||
            computed.storingType == ComputedAttStoringType.ON_CREATE && !isNewRecord
        ) {
            return
        }
        if (computed.storingType == ComputedAttStoringType.ON_EMPTY) {
            val currentValue = recordsService.getAtt(value, "$attId[]?raw")
            if (!isEmptyRawValue(currentValue)) {
                return
            }
        }
        if (computed.type == ComputedAttType.COUNTER) {
            if (!isNewRecord) {
                return
            }
            val templateRef = EntityRef.valueOf(computed.config[COUNTER_CONFIG_TEMPLATE_KEY].asText())
            if (EntityRef.isNotEmpty(templateRef)) {
                result[attId] = ecosNumService.getNextNumberForRecord(value, templateRef)
                return
            }
        }
        val recordComputedAtt = RecordComputedAtt(attId, computed.type.toRecordComputedType(), computed.config)
        result[attId] = recordComputedAttsService.compute(value, recordComputedAtt) { null }
    }

    private fun isEmptyRawValue(value: DataValue): Boolean {
        if (value.isNull()) {
            return true
        }
        if (value.isTextual()) {
            return value.asText().isEmpty()
        }
        if (value.isArray()) {
            return value.size() == 0 || value.all { isEmptyRawValue(it) }
        }
        return false
    }

    private class ValueWithData(value: AttValue, val data: Map<String, Any?>) : AttValueDelegate(value) {

        override fun getAtt(name: String): Any? {
            if (data.containsKey(name)) {
                return data[name]
            }
            return super.getAtt(name)
        }
    }
}
