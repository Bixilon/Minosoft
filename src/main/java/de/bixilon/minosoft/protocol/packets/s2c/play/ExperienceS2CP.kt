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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.entities.entities.player.local.ExperienceCondition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class ExperienceS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val bar = buffer.readFloat()
    val level: Int
    val total: Int

    init {
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            level = buffer.readUnsignedShort()
            total = buffer.readUnsignedShort()
        } else {
            level = buffer.readVarInt()
            total = buffer.readVarInt()
        }
    }

    override fun check(connection: PlayConnection) {
        check(bar in 0.0f..1.0f) { "Bar is invalid!" }
        check(level >= 0) { "Level is negative!" }
        check(total >= 0) { "Total experience is negative!" }
    }

    override fun handle(connection: PlayConnection) {
        connection.player.experienceCondition = ExperienceCondition(
            level = level,
            total = total,
            bar = bar,
        )
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Experience set (bar=$bar, level=$level, total=$total)" }
    }
}
