package ru.citeck.ecos.model.lib.workspace.api

interface WorkspaceApi {

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
    fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>>

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

    fun mapIdentifiers(identifiers: List<String>, mappingType: IdMappingType): List<String>

    enum class IdMappingType(val id: Int) {

        WS_SYS_ID_TO_ID(0),
        WS_ID_TO_SYS_ID(1),
        NO_MAPPING(-1);

        companion object {
            fun fromId(id: Int): IdMappingType {
                return entries.find { it.id == id } ?: NO_MAPPING
            }
        }
    }
}
