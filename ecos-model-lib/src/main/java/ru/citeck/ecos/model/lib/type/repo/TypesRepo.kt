package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface TypesRepo {

    fun getTypeInfo(typeRef: EntityRef): TypeInfo?

    fun getChildren(typeRef: EntityRef): List<EntityRef>

    /**
     * Subscribes to "this type has changed".
     *
     * An implementation **must not report its own loading as changes**. Filling a repository from
     * its storage is not a stream of events, however many writes it takes internally - a consumer
     * told otherwise would be handed the whole model as "changes" on every start, and could never
     * tell a type that was just created from one that has merely been read. What follows from that
     * rule is the meaning of the two ends of the event, and it is the reason the rule exists:
     *  - `before == null` means the type did not exist;
     *  - `after == null` means it was deleted.
     *
     * Both are null for an implementation that has the ids of changed types and nothing else, which
     * is why they are nullable rather than merely "the old and the new value".
     *
     * [before] and [after] can carry the **same** definition. A value re-saved with nothing changed
     * but its metadata is a change to the repository and not to the model, so an empty difference
     * here does not mean nothing happened - it means nothing happened *to the type*.
     *
     * The listener is called **synchronously on the publishing thread**, which may be a registry
     * watcher thread or the transaction that is saving the type. It must therefore return
     * immediately and must not throw: anything more than recording what changed belongs on the
     * consumer's own thread.
     *
     * Abstract on purpose, with no do-nothing body to inherit. A repository that has no events to
     * report says so by implementing this and never calling the listener - deliberately, in code
     * someone wrote - while a body here would let the same silence happen by omission, and silence
     * from a consumer's side is indistinguishable from a model that never changes.
     */
    fun listenTypeChanges(listener: (typeId: String, before: TypeInfo?, after: TypeInfo?) -> Unit)
}
