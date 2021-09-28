package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.records2.RecordRef

interface TypesRepo {

    fun getTypeInfo(typeRef: RecordRef): TypeInfo?

    fun getChildren(typeRef: RecordRef): List<RecordRef>
}
