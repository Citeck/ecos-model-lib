package ru.citeck.ecos.model.lib.role.dto

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.json.Json

class RoleDefTest {

    @Test
    fun test() {

        println(Json.mapper.toString(RoleDef.create().build()))


    }

}
