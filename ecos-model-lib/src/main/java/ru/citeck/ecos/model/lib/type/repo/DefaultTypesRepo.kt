package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.records2.RecordRef

class DefaultTypesRepo : TypesRepo {

    override fun getTypeDef(typeRef: RecordRef): TypeDef? = null

    override fun getChildren(typeRef: RecordRef): List<RecordRef> = emptyList()
}
