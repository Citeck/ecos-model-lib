package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAtt
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttType
import ru.citeck.ecos.records3.record.type.RecordTypeService

class RecordTypeServiceImpl(services: ModelServiceFactory) : RecordTypeService {

    private val typesRepo = services.typesRepo

    override fun getComputedAtts(typeRef: RecordRef): List<RecordComputedAtt> {
        val result = mutableListOf<RecordComputedAtt>()
        val typeAtts = typesRepo.getTypeInfo(typeRef)?.model?.getAllAttributes() ?: emptyList()
        for (att in typeAtts) {
            val computed = att.computed
            if (computed.storingType != ComputedAttStoringType.NONE) {
                continue
            }
            val recCompType = computed.type.toRecordComputedType()
            if (recCompType == RecordComputedAttType.NONE) {
                continue
            }
            result.add(RecordComputedAtt(att.id, recCompType, computed.config))
        }
        return result
    }
}
