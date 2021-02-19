package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.records2.RecordRef

interface TypesRepo {

    fun getModel(typeRef: RecordRef): TypeModelDef

    fun getParent(typeRef: RecordRef): RecordRef

    fun getChildren(typeRef: RecordRef): List<RecordRef>
}
