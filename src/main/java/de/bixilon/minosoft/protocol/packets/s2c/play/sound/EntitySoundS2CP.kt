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
package de.bixilon.minosoft.protocol.packets.s2c.play.sound

import de.bixilon.minosoft.data.SoundCategories
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntitySoundS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val soundEvent = buffer.readSound()
    val category: SoundCategories = SoundCategories[buffer.readVarInt()]
    val entityId: Int = buffer.readVarInt()
    val volume: Float = buffer.readFloat()
    val pitch: Float = buffer.readFloat()
    val seed: Long = if (buffer.versionId >= ProtocolVersions.V_22W14A) buffer.readLong() else 0L


    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Entity sound effect (soundEvent=$soundEvent, category=$category, entityId$entityId, volume=$volume, pitch=$pitch)" }
    }
}
