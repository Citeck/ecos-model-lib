package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType

const val USER_WORKSPACE_PREFIX = "user$"

interface WorkspaceService {

    /**
     * Retrieves a set of workspace identifiers where the specified user is a member.
     * This method loads **all** workspaces, regardless of whether the membership is direct or indirect.
     * Equivalent to calling [getUserWorkspaces] with [WsMembershipType.ALL].
     *
     * @param user the username for which workspaces are being retrieved
     * @return a set of all workspace identifiers where the user is a member
     */
    fun getUserWorkspaces(user: String): Set<String>

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

    /**
     * Checks whether the specified user is a member of the given workspace.
     *
     * @param user the username to check for member rights
     * @param workspace the identifier of the workspace
     * @return true if the user is a member of the workspace, false otherwise
     */
    fun isUserMemberOf(user: String, workspace: String): Boolean
}
