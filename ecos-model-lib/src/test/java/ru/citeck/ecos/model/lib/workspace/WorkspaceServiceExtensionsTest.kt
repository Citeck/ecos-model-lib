package ru.citeck.ecos.model.lib.workspace

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.webapp.api.entity.EntityRef

class WorkspaceServiceExtensionsTest {

    companion object {
        const val SYS_ID_POSTFIX = "-sys-id"
        const val WS = "custom"
        const val WS_PREFIX = "custom$SYS_ID_POSTFIX:"
    }

    private lateinit var workspaceService: WorkspaceService

    @Test
    fun `currentWs placeholder is rebound to target ws prefix`() {
        val ref = EntityRef.valueOf("emodel/type@CURRENT_WS:my-type")
        val result = workspaceService.bindRefToWorkspace(ref, WS)
        assertThat(result.toString()).isEqualTo("emodel/type@${WS_PREFIX}my-type")
    }

    @Test
    fun `currentWs placeholder is stripped to bare on global deploy`() {
        val ref = EntityRef.valueOf("emodel/type@CURRENT_WS:my-type")
        val result = workspaceService.bindRefToWorkspace(ref, "")
        assertThat(result.toString()).isEqualTo("emodel/type@my-type")
    }

    @Test
    fun `already ws-prefixed ref is unchanged`() {
        val ref = EntityRef.valueOf("emodel/type@${WS_PREFIX}my-type")
        val result = workspaceService.bindRefToWorkspace(ref, WS)
        assertThat(result.toString()).isEqualTo("emodel/type@${WS_PREFIX}my-type")
    }

    @Test
    fun `unprefixed ref is left alone when not co-deployed`() {
        val ref = EntityRef.valueOf("emodel/type@my-type")
        val result = workspaceService.bindRefToWorkspace(ref, WS)
        assertThat(result.toString()).isEqualTo("emodel/type@my-type")
    }

    @Test
    fun `unprefixed ref is promoted when co-deployed in target ws`() {
        val ref = EntityRef.valueOf("emodel/type@my-type")
        val coDeployed = setOf(
            EntityRef.valueOf("emodel/type@my-type"),
            EntityRef.valueOf("uiserv/form@my-form")
        )
        val result = workspaceService.bindRefToWorkspace(ref, WS, coDeployed)
        assertThat(result.toString()).isEqualTo("emodel/type@${WS_PREFIX}my-type")
    }

    @Test
    fun `unprefixed ref not in co-deployed set stays global`() {
        val ref = EntityRef.valueOf("notifications/template@global-template")
        val coDeployed = setOf(EntityRef.valueOf("emodel/type@my-type"))
        val result = workspaceService.bindRefToWorkspace(ref, WS, coDeployed)
        assertThat(result.toString()).isEqualTo("notifications/template@global-template")
    }

    @Test
    fun `empty ref is returned as-is`() {
        val result = workspaceService.bindRefToWorkspace(EntityRef.EMPTY, WS, setOf(EntityRef.EMPTY))
        assertThat(result).isEqualTo(EntityRef.EMPTY)
    }

    @BeforeEach
    fun beforeEach() {
        val modelServices = ModelServiceFactory()
        modelServices.setWorkspaceApi(object : WorkspaceApi {
            override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> =
                workspaces.map { emptySet() }
            override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> = emptySet()
            override fun isUserManagerOf(user: String, workspace: String): Boolean = false
            override fun mapIdentifiers(
                identifiers: List<String>,
                mappingType: WorkspaceApi.IdMappingType
            ): List<String> = when (mappingType) {
                WorkspaceApi.IdMappingType.WS_SYS_ID_TO_ID -> identifiers.map { it.replace(SYS_ID_POSTFIX, "") }
                WorkspaceApi.IdMappingType.WS_ID_TO_SYS_ID -> identifiers.map { it + SYS_ID_POSTFIX }
                else -> identifiers
            }
        })
        workspaceService = modelServices.workspaceService
    }
}
