package ru.citeck.ecos.model.lib.workspace

interface WorkspaceService {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member.
     * Returned set always contain user workspace ``"user$" + user``
     *
     * @param user the username for which workspaces are being retrieved
     * @return a set of workspace identifiers where the user is a member
     */
    fun getUserWorkspaces(user: String): Set<String>

    fun isUserManagerOf(user: String, workspace: String): Boolean
}
