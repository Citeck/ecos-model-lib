package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.records2.RecordRef

interface TypesRepo {

    fun getTypeDef(typeRef: RecordRef): TypeDef?

    fun getChildren(typeRef: RecordRef): List<RecordRef>
}
