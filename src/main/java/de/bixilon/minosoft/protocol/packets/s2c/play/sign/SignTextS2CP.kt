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
package de.bixilon.minosoft.protocol.packets.s2c.play.sign

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.block.sign.SignBlockEntity
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class SignTextS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3i = if (buffer.versionId < ProtocolVersions.V_14W04A) {
        buffer.readShortBlockPosition()
    } else {
        buffer.readBlockPosition()
    }
    val lines: Array<ChatComponent>


    init {
        val lines: MutableList<ChatComponent> = mutableListOf()
        for (i in 0 until SignBlockEntity.LINES) {
            lines.add(buffer.readChatComponent())
        }
        this.lines = lines.toTypedArray()
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.getBlockEntity(position)?.unsafeCast<SignBlockEntity>() ?: SignBlockEntity(connection)

        connection.world[position] = entity

        entity.lines = lines
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Sign text (position=$position, lines=$lines" }
    }
}
