package ru.citeck.ecos.model.lib.workspace.api

interface WorkspaceApi {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member,
     * based on their username and associated authorities.
     *
     * @param user the username for which workspaces are being retrieved
     * @param authorities a list of authorities or roles assigned to the user
     * @return a set of workspace identifiers where the user is a member
     */
    fun getUserWorkspaces(user: String, authorities: List<String>): Set<String>
}
