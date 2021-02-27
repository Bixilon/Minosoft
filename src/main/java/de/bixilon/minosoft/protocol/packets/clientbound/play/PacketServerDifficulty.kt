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

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log

class PacketServerDifficulty : ClientboundPacket() {
    lateinit var difficulty: Difficulties
    var locked = false

    override fun read(buffer: InByteBuffer): Boolean {
        difficulty = Difficulties.byId(buffer.readUnsignedByte().toInt())
        if (buffer.versionId > ProtocolVersions.V_19W11A) {
            locked = buffer.readBoolean()
        }
        return true
    }

    override fun handle(connection: Connection) {
        connection.player.world.difficulty = difficulty
        connection.player.world.difficultyLocked = locked
    }

    override fun log() {
        Log.protocol("[IN] Received server difficulty (difficulty=$difficulty, locked=${locked})")
    }
}
