package ru.citeck.ecos.model.lib.workspace

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType

class WorkspaceServiceTest {

    companion object {
        const val SYS_ID_POSTFIX = "-sys-id"
    }

    private lateinit var workspaceService: WorkspaceService
    private val nestedWorkspaces = HashMap<String, Set<String>>()

    @Test
    fun idInWsTest() {

        val idInWsAsText = "ws-0$SYS_ID_POSTFIX:custom"
        val idInWs = workspaceService.convertToIdInWs(idInWsAsText)
        assertThat(idInWs.workspace).isEqualTo("ws-0")
        assertThat(idInWs.id).isEqualTo("custom")
        assertThat(workspaceService.convertToStrId(idInWs)).isEqualTo(idInWsAsText)

        val idInWs2 = workspaceService.convertToIdInWs("w\$s-0$SYS_ID_POSTFIX:custom")
        assertThat(idInWs2.workspace).isEmpty()
        assertThat(idInWs2.id).isEqualTo("w\$s-0$SYS_ID_POSTFIX:custom")
    }

    @Test
    fun runAsWsSystemTest() {

        nestedWorkspaces["ws-1"] = setOf("ws-5", "ws-6")

        workspaceService.runAsWsSystem("ws-0") {
            val auth = AuthContext.getCurrentRunAsAuth()
            assertThat(auth.getUser()).isEqualTo("ws_system_ws-0$SYS_ID_POSTFIX")
            assertThat(auth.getAuthorities()).containsOnly(
                "ROLE_WS_SYSTEM",
                "ROLE_USER"
            )
            assertThat(workspaceService.isRunAsSystemOrWsSystem("ws-0")).isTrue()
            assertThat(workspaceService.isRunAsSystemOrWsSystem("ws-1")).isFalse()

            val workspaces = workspaceService.getAvailableWorkspacesToQuery(auth, listOf("ws-0", "ws-1", "ws-2"))
            assertThat(workspaces).containsExactly("ws-0")
            val workspaces2 = workspaceService.getAvailableWorkspacesToQuery(auth, listOf("ws-1", "ws-2"))
            assertThat(workspaces2).isNull()
            val workspaces3 = workspaceService.getAvailableWorkspacesToQuery(auth, emptyList())
            assertThat(workspaces3).containsExactly("ws-0", "")
            val workspaces4 = workspaceService.getAvailableWorkspacesToQuery(auth, listOf("", "default"))
            assertThat(workspaces4).containsExactly("")
        }

        workspaceService.runAsWsSystem("ws-1") {
            val auth = AuthContext.getCurrentRunAsAuth()
            val workspaces5 = workspaceService.getAvailableWorkspacesToQuery(auth, listOf("ws-1"))
            assertThat(workspaces5).containsExactly("ws-1", "ws-5", "ws-6")
        }

        AuthContext.runAsSystem {
            assertThat(workspaceService.isRunAsSystemOrWsSystem("ws-0")).isTrue()
            assertThat(workspaceService.isRunAsSystemOrWsSystem("ws-1")).isTrue()
        }
    }

    @BeforeEach
    fun beforeEach() {

        nestedWorkspaces.clear()
        val modelServices = ModelServiceFactory()

        modelServices.setWorkspaceApi(object : WorkspaceApi {
            override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> {
                return workspaces.map {
                    nestedWorkspaces[it] ?: emptySet()
                }
            }
            override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> = TODO()
            override fun isUserManagerOf(user: String, workspace: String): Boolean = TODO()
            override fun mapIdentifiers(
                identifiers: List<String>,
                mappingType: WorkspaceApi.IdMappingType
            ): List<String> {
                return when (mappingType) {
                    WorkspaceApi.IdMappingType.WS_SYS_ID_TO_ID -> identifiers.map { it.replace(SYS_ID_POSTFIX, "") }
                    WorkspaceApi.IdMappingType.WS_ID_TO_SYS_ID -> identifiers.map { it + SYS_ID_POSTFIX }
                    else -> identifiers
                }
            }
        })

        workspaceService = modelServices.workspaceService
    }
}
