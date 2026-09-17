package ru.citeck.ecos.model.lib.type.repo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

/**
 * The contract of [TypesRepo.listenTypeChanges], asserted from the outside.
 *
 * The method is abstract, so "an implementation forgot it" is a compile error and not something a
 * test can reach. What is left to assert is the shape of the event itself - that an id alone is
 * enough for an implementation that has nothing more, and that an implementation which does have
 * the definitions can hand over both sides of the change.
 */
class TypesRepoListenTest {

    @Test
    fun aRepoWithNothingToReportAcceptsTheListenerAndNeverCallsItTest() {
        var calls = 0
        DefaultTypesRepo().listenTypeChanges { _, _, _ -> calls++ }

        assertThat(calls).describedAs("a repo with no types must not invent an event").isZero
    }

    @Test
    fun anImplementationDeliversWhatChangedTest() {
        val received = ArrayList<Triple<String, TypeInfo?, TypeInfo?>>()
        val repo = EventfulTypesRepo()
        val docBefore = typeInfo("doc", "Old name")
        val docAfter = typeInfo("doc", "New name")

        repo.listenTypeChanges { id, before, after -> received.add(Triple(id, before, after)) }
        repo.publish("doc", docBefore, docAfter)
        repo.publish("case", null, typeInfo("case", "Case"))

        assertThat(received).containsExactly(
            Triple("doc", docBefore, docAfter),
            Triple("case", null, typeInfo("case", "Case"))
        )
    }

    /**
     * An implementation that knows which type changed and nothing more - the reason both sides of
     * the event are nullable rather than "the old and the new value". A consumer must be able to
     * work against this one, so the ids have to be enough on their own.
     */
    @Test
    fun anImplementationWithNoDefinitionsToGiveStillReportsTheIdTest() {
        val received = ArrayList<Triple<String, TypeInfo?, TypeInfo?>>()
        val repo = EventfulTypesRepo()

        repo.listenTypeChanges { id, before, after -> received.add(Triple(id, before, after)) }
        repo.publish("doc", null, null)

        assertThat(received).containsExactly(Triple("doc", null, null))
    }

    private fun typeInfo(id: String, name: String): TypeInfo {
        return TypeInfo.create {
            withId(id)
            withName(MLText(name))
        }
    }

    private class EventfulTypesRepo : TypesRepo {

        private val listeners = ArrayList<(String, TypeInfo?, TypeInfo?) -> Unit>()

        override fun getTypeInfo(typeRef: EntityRef): TypeInfo? = null

        override fun getChildren(typeRef: EntityRef): List<EntityRef> = emptyList()

        override fun listenTypeChanges(
            listener: (typeId: String, before: TypeInfo?, after: TypeInfo?) -> Unit
        ) {
            listeners.add(listener)
        }

        fun publish(typeId: String, before: TypeInfo?, after: TypeInfo?) {
            listeners.forEach { it.invoke(typeId, before, after) }
        }
    }
}
