package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.records2.RecordRef

class DefaultTypesRepo : TypesRepo {

    override fun getModel(typeRef: RecordRef): TypeModelDef = TypeModelDef.EMPTY

    override fun getParent(typeRef: RecordRef): RecordRef = RecordRef.EMPTY

    override fun getChildren(typeRef: RecordRef): List<RecordRef> = emptyList()
}
