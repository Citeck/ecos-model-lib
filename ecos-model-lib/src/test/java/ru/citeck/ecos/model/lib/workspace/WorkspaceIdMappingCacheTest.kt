package ru.citeck.ecos.model.lib.workspace

import com.github.benmanes.caffeine.cache.Ticker
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lifetime of cached workspace identifier mappings. See COREDEV-514: while an empty mapping is
 * cached, every reference prefixed with that workspace system id resolves to the global scope and
 * all artifacts of the workspace are read as non-existent, so an answer which resolved nothing
 * must not live as long as a real one.
 */
class WorkspaceIdMappingCacheTest {

    private val ticker = FakeTicker()

    /** wsId -> wsSysId, the only mapping the fake api knows about */
    private val sysIdByWsId = HashMap<String, String>()
    private val loadsCount = AtomicInteger()
    private var failOnNextLoad = false

    private val workspaceService: WorkspaceService = createWorkspaceService()

    @Test
    fun unresolvedMappingIsCheckedAgainWithoutWaitingForResolvedTtl() {

        assertThat(workspaceService.getWorkspaceIdBySystemId("ws-9-sys")).isEmpty()
        assertThat(loadsCount.get()).isEqualTo(1)

        sysIdByWsId["ws-9"] = "ws-9-sys"

        // reading the empty answer must not prolong its life
        ticker.advance(Duration.ofSeconds(20))
        assertThat(workspaceService.getWorkspaceIdBySystemId("ws-9-sys")).isEmpty()
        assertThat(loadsCount.get()).isEqualTo(1)

        ticker.advance(Duration.ofSeconds(11))
        assertThat(workspaceService.getWorkspaceIdBySystemId("ws-9-sys")).isEqualTo("ws-9")
    }

    @Test
    fun resolvedMappingSurvivesFrequentReads() {

        sysIdByWsId["ws-1"] = "ws-1-sys"

        assertThat(workspaceService.getWorkspaceIdBySystemId("ws-1-sys")).isEqualTo("ws-1")
        assertThat(loadsCount.get()).isEqualTo(1)

        // two minutes of reads spaced by less than the access ttl - still a single load
        repeat(6) {
            ticker.advance(Duration.ofSeconds(20))
            assertThat(workspaceService.getWorkspaceIdBySystemId("ws-1-sys")).isEqualTo("ws-1")
        }
        assertThat(loadsCount.get()).isEqualTo(1)
    }

    /**
     * A generated "DELETED_..." system id is a valid answer of the model app, but it is not a
     * mapping to a real system id: keeping it would make artifacts of the workspace created later
     * get identifiers with that generated prefix.
     */
    @Test
    fun generatedSystemIdIsNotKeptAsResolvedOne() {

        assertThat(workspaceService.getWorkspaceSystemId("ws-2")).startsWith("DELETED_")

        sysIdByWsId["ws-2"] = "ws-2-sys"

        // reading the generated identifier must not prolong its life either
        ticker.advance(Duration.ofSeconds(20))
        assertThat(workspaceService.getWorkspaceSystemId("ws-2")).startsWith("DELETED_")

        ticker.advance(Duration.ofSeconds(11))
        assertThat(workspaceService.getWorkspaceSystemId("ws-2")).isEqualTo("ws-2-sys")
    }

    @Test
    fun failedLookupIsNotCached() {

        failOnNextLoad = true
        assertThatThrownBy {
            workspaceService.getWorkspaceIdBySystemId("ws-3-sys")
        }.hasMessageContaining("model app is not available")

        sysIdByWsId["ws-3"] = "ws-3-sys"
        assertThat(workspaceService.getWorkspaceIdBySystemId("ws-3-sys")).isEqualTo("ws-3")
    }

    private fun createWorkspaceService(): WorkspaceService {
        val services = object : ModelServiceFactory() {
            override fun createWorkspaceService(): WorkspaceService {
                return WorkspaceServiceImpl(this, ticker)
            }
        }
        services.setWorkspaceApi(
            object : WorkspaceApi {

                override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> {
                    return workspaces.map { emptySet() }
                }

                override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> = TODO()

                override fun isUserManagerOf(user: String, workspace: String): Boolean = TODO()

                override fun mapIdentifiers(
                    identifiers: List<String>,
                    mappingType: WorkspaceApi.IdMappingType
                ): List<String> {
                    loadsCount.incrementAndGet()
                    if (failOnNextLoad) {
                        failOnNextLoad = false
                        error("model app is not available")
                    }
                    return when (mappingType) {
                        WorkspaceApi.IdMappingType.WS_SYS_ID_TO_ID -> identifiers.map { sysId ->
                            sysIdByWsId.entries.find { it.value == sysId }?.key ?: ""
                        }
                        WorkspaceApi.IdMappingType.WS_ID_TO_SYS_ID -> identifiers.map { wsId ->
                            // the model app answers with a generated identifier
                            // when the workspace doesn't have a system id of its own
                            sysIdByWsId[wsId] ?: "DELETED_$wsId"
                        }
                        else -> identifiers
                    }
                }
            }
        )
        return services.workspaceService
    }

    private class FakeTicker : Ticker {

        @Volatile
        private var nanos: Long = 0

        override fun read(): Long = nanos

        fun advance(duration: Duration) {
            nanos += duration.toNanos()
        }
    }
}
