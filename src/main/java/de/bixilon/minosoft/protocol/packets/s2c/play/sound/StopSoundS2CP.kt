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

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.data.SoundCategories
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class StopSoundS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val category: SoundCategories?
    val sound: ResourceLocation?

    init {
        var category: SoundCategories? = null
        var sound: ResourceLocation? = null
        if (buffer.versionId < ProtocolVersions.V_17W45A) { // ToDo: these 2 values need to be switched in before 1.12.2
            category = SoundCategories.valueOf(buffer.readString().uppercase())
            sound = buffer.readResourceLocation()
        } else {
            val flags = buffer.readByte()
            if (isBitMask(flags.toInt(), 0x01)) {
                category = SoundCategories[buffer.readVarInt()]
            }
            if (isBitMask(flags.toInt(), 0x02)) {
                sound = buffer.readResourceLocation()
            }
        }
        this.category = category
        this.sound = sound
    }

    override fun handle(connection: PlayConnection) {
        sound?.let { connection.world.stopSound(it) }
        // ToDo: Category
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Stop sound (category=$category, sound=$sound)" }
    }
}
