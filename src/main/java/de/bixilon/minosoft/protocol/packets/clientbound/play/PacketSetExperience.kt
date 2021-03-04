/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.modding.event.events.ExperienceChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log

class PacketSetExperience : ClientboundPacket() {
    var bar = 0f
        private set
    var level = 0
        private set
    var total = 0
        private set

    override fun read(buffer: InByteBuffer): Boolean {
        bar = buffer.readFloat()
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            level = buffer.readUnsignedShort()
            total = buffer.readUnsignedShort()
            return true
        }
        level = buffer.readVarInt()
        total = buffer.readVarInt()
        return true
    }

    override fun check(connection: Connection) {
        check(bar in 0.0f..1.0f) { "Bar is invalid!" }
        check(level >= 0) { "Level is negative is invalid!" }
        check(total >= 0) { "Total experience is negative!" }
    }

    override fun handle(connection: Connection) {
        if (connection.fireEvent(ExperienceChangeEvent(connection, this))) {
            return
        }
        connection.player.level = level
        connection.player.experienceBarProgress = bar
        connection.player.totalExperience = total
    }

    override fun log() {
        Log.protocol("[IN] Level update received. Now at $level level(s), total $total experience, experience bar at $bar", level, total)
    }
}
