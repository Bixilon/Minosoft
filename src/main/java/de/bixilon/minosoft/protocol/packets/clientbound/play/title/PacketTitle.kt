/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play.title

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.TitleChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log

class PacketTitle() : ClientboundPacket() {
    lateinit var action: TitleActions

    // fields depend on action
    var text: ChatComponent? = null
    var subText: ChatComponent? = null
    var fadeInTime = 0
    var stayTime = 0
    var fadeOutTime = 0

    constructor(buffer: InByteBuffer) : this() {
        action = buffer.connection.mapping.titleActionsRegistry.get(buffer.readVarInt())!!
        when (action) {
            TitleActions.SET_TITLE -> this.text = buffer.readChatComponent()
            TitleActions.SET_SUBTITLE -> this.subText = buffer.readChatComponent()
            TitleActions.SET_TIMES_AND_DISPLAY -> {
                this.fadeInTime = buffer.readInt()
                this.stayTime = buffer.readInt()
                this.fadeOutTime = buffer.readInt()
            }
            else -> {
            }
        }
    }

    override fun handle(connection: Connection) {
        if (connection.fireEvent(TitleChangeEvent(connection, this))) {
            return
        }
    }

    override fun log() {
        when (this.action) {
            TitleActions.SET_TITLE -> Log.protocol(String.format("[IN] Received title (action=%s, text=%s)", this.action, this.text?.ansiColoredMessage))
            TitleActions.SET_SUBTITLE -> Log.protocol(String.format("[IN] Received title (action=%s, subText=%s)", this.action, this.subText?.ansiColoredMessage))
            TitleActions.SET_TIMES_AND_DISPLAY -> Log.protocol(String.format("[IN] Received title (action=%s, fadeInTime=%d, stayTime=%d, fadeOutTime=%d)", this.action, this.fadeInTime, this.stayTime, this.fadeOutTime))
            TitleActions.HIDE, TitleActions.RESET -> Log.protocol(String.format("[IN] Received title (action=%s)", this.action))
        }
    }

    enum class TitleActions {
        SET_TITLE,
        SET_SUBTITLE,
        SET_ACTION_BAR,
        SET_TIMES_AND_DISPLAY,
        HIDE,
        RESET,
        ;

        companion object : ValuesEnum<TitleActions> {
            override val VALUES = values()
            override val NAME_MAP = KUtil.getEnumValues(VALUES)
        }
    }
}
