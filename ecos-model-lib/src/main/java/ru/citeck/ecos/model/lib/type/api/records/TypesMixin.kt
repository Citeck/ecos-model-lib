package ru.citeck.ecos.model.lib.type.api.records

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.mixin.AttMixin

class TypesMixin(services: ModelServiceFactory) : AttMixin {

    private val service = services.typeRefService

    override fun getAtt(path: String, value: AttValueCtx): Any {
        return IsSubType(TypeUtils.getTypeRef(value.getLocalId()))
    }

    override fun getProvidedAtts(): Collection<String> {
        return listOf(TypeConstants.ATT_IS_SUBTYPE_OF)
    }

    inner class IsSubType(private val recType: RecordRef) : AttValue {

        override fun getAtt(name: String): Boolean {
            val typeRef = if (!name.contains("@")) {
                TypeUtils.getTypeRef(name)
            } else {
                RecordRef.valueOf(name)
            }
            return service.isSubType(recType, typeRef)
        }
    }
}
