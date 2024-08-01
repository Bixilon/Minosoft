/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block.sign

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.language.lang.LanguageFile
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.IT
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["block_entity"])
class SignBlockEntityTest {

    private fun create(): SignBlockEntity {
        val session = IT.OBJENESIS.newInstance(PlaySession::class.java)
        session::language.forceSet(LanguageFile("", Namespaces.MINECRAFT, mutableMapOf()))
        return SignBlockEntity(session)
    }

    fun `nbt 1_20_2`() {
        val nbt = mapOf(
            "is_waxed" to 1.toByte(),
            "front_text" to mapOf(
                "has_glowing_text" to 1.toByte(),
                "color" to "red",
                "messages" to listOf(
                    """{"text":"This is the front"}""",
                    """{"text":"text"}""",
                    """{"text":"of"}""",
                    """{"text":"this sign."}""",
                )
            ),
            "back_text" to mapOf(
                "has_glowing_text" to 0.toByte(),
                "color" to "blue",
                "messages" to listOf(
                    """{"text":"This is the back"}""",
                    """{"text":"text"}""",
                    """{"text":"of"}""",
                    """{"text":"this sign."}""",
                )
            ),
        )
        val entity = create()
        entity.updateNBT(nbt)

        assertTrue(entity.waxed)
        assertTrue(entity.front.glowing)
        assertEquals(entity.front.color, ChatColors.RED)
        assertEquals(entity.front.text, arrayOf(ChatComponent.of("This is the front"), ChatComponent.of("text"), ChatComponent.of("of"), ChatComponent.of("this sign.")))


        assertFalse(entity.back.glowing)
        assertEquals(entity.back.color, ChatColors.BLUE)
        assertEquals(entity.back.text, arrayOf(ChatComponent.of("This is the back"), ChatComponent.of("text"), ChatComponent.of("of"), ChatComponent.of("this sign.")))
    }
    fun `nbt 1_20_4`() {
        val nbt = mapOf(
            "is_waxed" to 1.toByte(),
            "front_text" to mapOf(
                "has_glowing_text" to 1.toByte(),
                "color" to "red",
                "messages" to listOf(
                    "\"Very long line\"",
                    "\"\"",
                    "\"\"",
                    "\"\"",
                )
            ),
        )
        val entity = create()
        entity.updateNBT(nbt)

        assertTrue(entity.waxed)
        assertTrue(entity.front.glowing)
        assertEquals(entity.front.color, ChatColors.RED)
        assertEquals(entity.front.text, arrayOf(TextComponent("Very long line"), ChatComponent.of(""), ChatComponent.of(""), ChatComponent.of("")))
    }
}
