package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType

const val USER_WORKSPACE_PREFIX = "user$"

interface WorkspaceService {

    /**
     * Expands the given list of workspaces by including all their nested workspaces.
     *
     * @param workspaces the workspace identifiers to expand
     * @return a set containing the original workspaces and all their nested workspaces
     */
    fun expandWorkspaces(workspaces: Collection<String>): Set<String>

    /**
     * Returns nested workspaces for the given list of workspaces.
     *
     * The returned list has the same size as the input.
     * Each element at index `i` contains the nested workspaces
     * for the workspace at index `i` in the input.
     *
     * @param workspaces the list of workspace identifiers to get nested workspaces for
     * @return a list where each element contains the nested workspaces for the corresponding input workspace
     */
    fun getNestedWorkspaces(workspaces: List<String>): List<Set<String>>

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

    fun isWorkspaceWithGlobalArtifacts(workspace: String?): Boolean

    fun getWorkspaceSystemId(workspace: String): String

    fun getWorkspaceSystemId(workspaces: List<String>): List<String>

    fun getWorkspaceIdBySystemId(workspaceSysId: String): String

    fun getWorkspaceIdBySystemId(workspaceSysId: List<String>): List<String>

    fun removeWsPrefixFromId(id: String, workspace: String): String

    fun addWsPrefixToId(localId: String, workspace: String): String

    fun resetNestedWorkspacesCache()

    fun resetNestedWorkspacesCache(workspaces: Collection<String>)

    fun convertToIdInWs(strId: String): IdInWs

    fun convertToStrId(idInWs: IdInWs): String

    fun getArtifactsWritePermission(user: String, workspace: String?, typeId: String): Boolean

    fun getUpdatedWsInMutation(currentWs: String, ctxWorkspace: String?): String
}
