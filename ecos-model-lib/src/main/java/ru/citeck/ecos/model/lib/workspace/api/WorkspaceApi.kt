package ru.citeck.ecos.model.lib.workspace.api

interface WorkspaceApi {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member,
     * filtered by the specified membership type.
     *
     * @param user the username for which workspaces are being retrieved
     * @param membershipType the type of membership to consider
     * @return a set of workspace identifiers matching the membership type
     */
    fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String>

    /**
     * Checks whether the specified user is a manager of the given workspace.
     *
     * @param user the username to check for manager rights
     * @param workspace the identifier of the workspace
     * @return true if the user is a manager of the workspace, false otherwise
     */
    fun isUserManagerOf(user: String, workspace: String): Boolean
}
