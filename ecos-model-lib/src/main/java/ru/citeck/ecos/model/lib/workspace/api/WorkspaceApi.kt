package ru.citeck.ecos.model.lib.workspace.api

interface WorkspaceApi {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member.
     *
     * @param user the username for which workspaces are being retrieved
     * @return a set of workspace identifiers where the user is a member
     */
    fun getUserWorkspaces(user: String): Set<String>
}
