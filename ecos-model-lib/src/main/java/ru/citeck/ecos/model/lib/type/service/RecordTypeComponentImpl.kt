package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttStoringType
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAtt
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttType
import ru.citeck.ecos.records3.record.type.RecordTypeComponent
import ru.citeck.ecos.records3.record.type.RecordTypeInfo
import ru.citeck.ecos.records3.record.type.RecordTypeInfoAdapter
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RecordTypeComponentImpl(services: ModelServiceFactory) : RecordTypeComponent {

    private val typesRepo = services.typesRepo

    override fun getRecordType(typeRef: EntityRef): RecordTypeInfo? {
        return RecordTypeInfoImpl(typeRef)
    }

    inner class RecordTypeInfoImpl(private val typeRef: EntityRef) : RecordTypeInfoAdapter() {

        private val typeInfo: TypeInfo? by lazy {
            typesRepo.getTypeInfo(typeRef)
        }

        private val computedAttsValue: List<RecordComputedAtt> by lazy {
            val result = mutableListOf<RecordComputedAtt>()
            val typeAtts = typeInfo?.model?.getAllAttributes() ?: emptyList()
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
            result
        }

        override fun getComputedAtts(): List<RecordComputedAtt> {
            return computedAttsValue
        }

        override fun getSourceId(): String {
            return typeInfo?.sourceId ?: ""
        }
    }
}
