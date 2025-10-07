package ru.citeck.ecos.model.lib.workspace

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType

class WorkspaceServiceTest {

    companion object {
        const val SYS_ID_POSTFIX = "-sys-id"
    }

    private lateinit var workspaceService: WorkspaceService

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

    @BeforeEach
    fun beforeEach() {

        val modelServices = ModelServiceFactory()

        modelServices.setWorkspaceApi(object : WorkspaceApi {
            override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> = TODO()
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
