package ru.citeck.ecos.model.lib.role.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.Json
import java.util.*

class RoleDefTest {

    @Test
    fun fullDefTest() {

        val roleDef = RoleDef(
            "role-id",
            MLText(Locale.ENGLISH to "en", Locale("ru") to "ru"),
            listOf("attribute0", "attribute1"),
            listOf("assignee0", "assignee1")
        )
        assertThat(roleDef).isEqualTo(RoleDef.Builder(roleDef).build())

        val roleByBuilder = RoleDef.create()
            .withId(roleDef.id)
            .withName(roleDef.name)
            .withAttributes(roleDef.attributes)
            .withAssignees(roleDef.assignees)
            .build()
        assertThat(roleByBuilder).isEqualTo(roleDef)

        val defFromRead = Json.mapper.read(Json.mapper.toString(roleDef), RoleDef::class.java)!!
        assertThat(defFromRead).isEqualTo(roleDef)
    }

    @Test
    fun attributesTest() {

        val attribute0 = "attribute0"
        val attribute1 = "attribute1"

        val roleDef0 = RoleDef.create()
            .withAttribute(attribute0)
            .build()

        assertThat(roleDef0.attributes).containsExactly(attribute0)

        val roleDef1 = RoleDef.create()
            .withAttributes(listOf(attribute0, attribute1))
            .build()

        assertThat(roleDef1.attributes).containsExactly(attribute0, attribute1)

        val defFromStrSingleAtt = Json.mapper.read("""
            {
                "id": "abc",
                "attribute": "$attribute0"
            }
        """.trimIndent(), RoleDef::class.java)!!

        assertThat(defFromStrSingleAtt.attributes).containsExactly(attribute0)

        val defFromStrArrayAtt = Json.mapper.read("""
            {
                "id": "abc",
                "attributes": ["$attribute0", "$attribute1"]
            }
        """.trimIndent(), RoleDef::class.java)!!

        assertThat(defFromStrArrayAtt.attributes).containsExactly(attribute0, attribute1)
    }
}
