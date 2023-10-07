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

package de.bixilon.minosoft.protocol.packets.s2c.play.chat

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.chat.sender.UnknownMessageSender
import de.bixilon.minosoft.data.entities.entities.player.tab.TabList
import de.bixilon.minosoft.data.language.lang.LanguageList
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.chat.TypeProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketReadingTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["packet"])
class SignedChatMessageS2CPTest {

    private fun connection(): PlayConnection {
        val connection = createConnection()
        connection::language.forceSet(LanguageList(mutableListOf()))
        connection::tabList.forceSet(TabList())
        connection.registries.messageType[0] = ChatMessageType(minecraft("dummy"), TypeProperties("test", emptyList(), emptyMap()), null, ChatTextPositions.CHAT)

        return connection
    }

    fun vanilla_23w40a() {
        val packet = PacketReadingTestUtil.read("signed_chat_message/vanilla_23w40a", "23w40a", connection(), constructor = ::SignedChatMessageS2CP)
        assertEquals(packet.message.message, "abc")
        assertEquals(packet.message.sender, UnknownMessageSender("a21a6c65-bbd4-48ca-9d79-e07139e1780d".toUUID()))
    }
}

