package ru.citeck.ecos.model.lib.workspace

import ru.citeck.ecos.context.lib.auth.data.AuthData
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.records2.predicate.model.Predicate

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
     * Returns a set of workspace identifiers for the given user or workspace-system user.
     *
     * If the provided [auth] represents a workspace-system user, the workspaces associated
     * with that system user are returned. Otherwise, workspaces for the regular user are returned.
     *
     * @param auth authentication data determining which user (regular or ws-system)
     *             the lookup should be performed for
     * @return a set of workspaces available to the user, or `null` if no workspaces can be resolved
     */
    fun getUserOrWsSystemUserWorkspaces(auth: AuthData): Set<String>?

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

    /**
     * Checks whether the given workspace contains globally shared entities
     * (i.e., entities not scoped to a specific workspace).
     *
     * @param workspace the workspace identifier, may be null
     * @return true if the workspace stores global entities, false otherwise
     */
    fun isWorkspaceWithGlobalEntities(workspace: String?): Boolean

    /**
     * Returns the system identifier (systemId) of the given workspace.
     *
     * @param workspace the workspace identifier, may be null
     * @return the system identifier of the workspace. Return empty string when system id doesn't exist.
     */
    fun getWorkspaceSystemId(workspace: String?): String

    /**
     * Returns system identifiers (systemId) for the given list of workspaces.
     *
     * @param workspaces workspace identifiers
     * @return list of corresponding system identifiers.
     */
    fun getWorkspaceSystemId(workspaces: List<String>): List<String>


    /**
     * Resolves the workspace identifier by its system identifier.
     *
     * @param workspaceSysId the system identifier of a workspace
     * @return the corresponding workspace identifier
     */
    fun getWorkspaceIdBySystemId(workspaceSysId: String): String

    /**
     * Resolves workspace identifiers for the given list of system identifiers.
     *
     * @param workspaceSysId system identifiers
     * @return list of corresponding workspace identifiers
     */
    fun getWorkspaceIdBySystemId(workspaceSysId: List<String>): List<String>

    /**
     * Removes the workspace prefix from the given record identifier.
     *
     * Example: `"ws-sys-id:abc"` → `"abc"`
     *
     * @param id the full identifier containing prefix
     * @param workspace the workspace whose prefix should be removed
     * @return the identifier without workspace prefix
     */
    fun removeWsPrefixFromId(id: String, workspace: String): String

    /**
     * Adds the workspace prefix to the given local record identifier.
     *
     * Example: `"abc"` → `"ws-sys-id:abc"`
     *
     * @param localId the local identifier without workspace prefix
     * @param workspace the workspace to prefix with
     * @return the full identifier including workspace prefix
     */
    fun addWsPrefixToId(localId: String, workspace: String): String

    /**
     * Clears the entire nested-workspaces cache.
     *
     * Should be called when workspace relationships change.
     */
    fun resetNestedWorkspacesCache()

    /**
     * Clears cached nested-workspaces data only for the given collection of workspaces.
     *
     * @param workspaces the workspaces whose cached data should be invalidated
     */
    fun resetNestedWorkspacesCache(workspaces: Collection<String>)

    /**
     * Converts a string identifier into its structured workspace-aware representation.
     *
     * @param strId a full identifier that may contain a workspace prefix
     * @return structured representation [IdInWs]
     */
    fun convertToIdInWs(strId: String): IdInWs

    /**
     * Converts a structured workspace-aware identifier to its string representation.
     *
     * @param idInWs structured identifier
     * @return string representation with workspace prefix if applicable
     */
    fun convertToStrId(idInWs: IdInWs): String


    /**
     * Checks whether the user has permission to write artifacts
     * of the specified type in the given workspace.
     *
     * @param user username
     * @param workspace workspace identifier, or null for global scope
     * @param typeId artifact type identifier
     * @return true if the user has write permission, false otherwise
     */
    fun getArtifactsWritePermission(user: String, workspace: String?, typeId: String): Boolean

    /**
     * Determines the workspace that should be used for a mutation operation,
     * based on the current workspace and the workspace from the execution context.
     *
     * @param currentWs the workspace associated with the entity being mutated
     * @param ctxWorkspace the workspace taken from the execution context
     *
     * @return the workspace identifier that should be applied to the mutated entity.
     */
    fun getUpdatedWsInMutation(currentWs: String, ctxWorkspace: String?): String

    /**
     * Builds a [Predicate] that filters entities based on the workspaces available
     * to the specified [auth].
     *
     * The resulting predicate ensures that only entities belonging to workspaces
     * accessible by the given user are included. If [queriedWorkspaces] is not empty,
     * the filter will also restrict results to those workspaces that both:
     *  - are listed in [queriedWorkspaces], and
     *  - are accessible to the [auth].
     *
     * @param auth authentication whose access rights
     *             should be considered when constructing the predicate
     * @param queriedWorkspaces a list of workspace identifiers that should be included
     *                          in the filtering scope; may be empty to include all
     *                          accessible workspaces
     * @return a [Predicate] suitable for use in query construction that enforces
     *         workspace-level access control. May return Predicates.alwaysFalse() and Predicates.alwaysTrue()
     */
    fun buildAvailableWorkspacesPredicate(auth: AuthData, queriedWorkspaces: List<String>): Predicate

    /**
     * Returns a set of workspaces available for querying for the given [auth].
     *
     * If [queriedWorkspaces] is empty, all accessible workspaces are returned.
     * Otherwise, the returned set is the intersection between accessible workspaces
     * and explicitly requested ones.
     *
     * @param auth authentication data of the user
     * @param queriedWorkspaces explicit workspace filter, may be empty
     * @return a set of workspaces available to query, or `null` if no workspaces are allowed
     */
    fun getAvailableWorkspacesToQuery(auth: AuthData, queriedWorkspaces: List<String>): Set<String>?

    /**
     * Checks whether the current execution is running under system or workspace-system privileges.
     *
     * @param workspace the workspace to check against
     * @return true if execution is under system/ws-system authority
     */
    fun isRunAsSystemOrWsSystem(workspace: String?): Boolean

    /**
     * Checks whether the given [auth] represents either system or workspace-system user.
     *
     * @param auth authentication data
     * @param workspace optional workspace to validate scope
     * @return true if the auth belongs to a system or ws-system user
     */
    fun isSystemOrWsSystemAuth(auth: AuthData, workspace: String?): Boolean

    /**
     * Checks whether the given [auth] represents system, workspace-system,
     * or a user with administrative rights.
     *
     * @param auth authentication data
     * @param workspace optional workspace to validate scope
     * @return true if the user has system, ws-system, or admin rights
     */
    fun isSystemOrWsSystemOrAdminAuth(auth: AuthData, workspace: String?): Boolean

    /**
     * Executes the given [action] under the workspace-system identity
     * belonging to the specified workspace.
     *
     * All operations performed inside [action] will behave as if invoked
     * by the workspace-system user of [workspace].
     *
     * @param workspace workspace whose system identity should be used
     * @param action code to execute under workspace-system privileges
     * @return the result of [action]
     */
    fun <T> runAsWsSystem(workspace: String, action: () -> T): T

    /**
     * Executes the given [action] under the workspace-system identity,
     * identified by the workspace system ID.
     *
     * Same as [runAsWsSystem], but takes `wsSysId` instead of workspace ID.
     *
     * @param wsSysId system identifier of the workspace
     * @param action code to execute under workspace-system privileges
     * @return the result of [action]
     */
    fun <T> runAsWsSystemBySystemId(wsSysId: String, action: () -> T): T
}
