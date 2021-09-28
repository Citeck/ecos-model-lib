package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.records2.RecordRef

class DefaultTypesRepo : TypesRepo {

    override fun getTypeInfo(typeRef: RecordRef): TypeInfo? = null

    override fun getChildren(typeRef: RecordRef): List<RecordRef> = emptyList()
}
