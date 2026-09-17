package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

/**
 * The repository of an application that has no model: every type is unknown and nothing ever
 * changes.
 *
 * `open` so that a caller who needs one or two of these answers and not the rest can extend it
 * instead of implementing [TypesRepo] from scratch. That is a deliberate opt-in to the empty
 * answers - unlike a body on the interface itself, which would give them to an implementation that
 * never asked and would hide the very silence [TypesRepo.listenTypeChanges] is written to expose.
 */
open class DefaultTypesRepo : TypesRepo {

    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? = null

    override fun getChildren(typeRef: EntityRef): List<EntityRef> = emptyList()

    /**
     * A repo with no types has no changes to report, so the listener is accepted and never called.
     */
    override fun listenTypeChanges(listener: (typeId: String, before: TypeInfo?, after: TypeInfo?) -> Unit) {
        // nothing ever changes here
    }
}
