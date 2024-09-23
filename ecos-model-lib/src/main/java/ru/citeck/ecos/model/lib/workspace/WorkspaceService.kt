package ru.citeck.ecos.model.lib.workspace

interface WorkspaceService {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member,
     * based on their username and associated authorities.
     * Returned set always contain user workspace ``"user$" + user``
     *
     * @param user the username for which workspaces are being retrieved
     * @param authorities a list of authorities or roles assigned to the user
     * @return a set of workspace identifiers where the user is a member
     */
    fun getUserWorkspaces(user: String, authorities: Collection<String>): Set<String>
}
