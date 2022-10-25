/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.chat

import de.bixilon.minosoft.data.chat.sender.*
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

object ChatUtil {
    val DEFAULT_CHAT_COLOR = ChatColors.WHITE

    fun PlayConnection.getMessageSender(uuid: UUID): MessageSender {
        val entity = this.world.entities[uuid]
        if (entity == null) {
            val tab = tabList.uuid[uuid] ?: return UnknownMessageSender(uuid)
            return UnspawnedMessageSender(uuid, tab)
        }
        if (entity !is PlayerEntity) {
            return InvalidSender(uuid)
        }
        return PlayerEntityMessageSender(uuid, entity.name, entity)
    }
}
