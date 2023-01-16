/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.chat

import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatMessageTypeTest {

    @Test
    fun decode1_19() {
        val data = mutableMapOf(
            "chat" to mutableMapOf(
                "decoration" to mutableMapOf(
                    "translation_key" to "chat.type.text",
                    "style" to emptyMap<String, Any>(),
                    "parameters" to listOf("sender", "content")
                )
            )
        )
        val type = ChatMessageType.deserialize(null, minosoft("empty"), data)
        assertEquals(type.chat, TypeProperties("chat.type.text", listOf(ChatParameter.SENDER, ChatParameter.CONTENT), emptyMap()))
        assertEquals(type.position, ChatTextPositions.CHAT)
        assertNull(type.narration)
    }
}
