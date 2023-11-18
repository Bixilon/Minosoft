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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.SoundCategories
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.modding.event.events.PlaySoundEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W02A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W15A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W18A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W14A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class SoundEventS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    var category: SoundCategories? = null
        private set
    val position: Vec3d
    val sound: ResourceLocation
    val volume: Float
    val pitch: Float
    var seed: Long = 0L
        private set
    var attenuationDistance: Float? = null
        private set

    init {
        if (buffer.versionId in V_17W15A until V_17W18A) {
            this.category = SoundCategories[buffer.readVarInt()]
        }
        if (buffer.versionId < ProtocolVersions.V_1_19_3_PRE3) {
            sound = buffer.readRegistryItem(buffer.connection.registries.soundEvent)
        } else {
            val id = buffer.readVarInt()
            if (id == 0) {
                // TODO: this was removed at some point
                sound = buffer.readResourceLocation()
                attenuationDistance = buffer.readOptional { readFloat() }
            } else {
                sound = buffer.connection.registries.soundEvent[id - 1]
            }
        }

        if (buffer.versionId in V_17W15A until V_17W18A) {
            buffer.readString() // parrot entity type
        }
        if (buffer.versionId >= V_16W02A && (buffer.versionId !in V_17W15A until V_17W18A)) {
            this.category = SoundCategories[buffer.readVarInt()]
        }
        position = Vec3d(buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4)
        volume = buffer.readFloat()
        pitch = buffer.readSoundPitch()

        if (buffer.versionId >= V_22W14A) {
            seed = buffer.readLong()
        }
    }

    override fun handle(connection: PlayConnection) {
        if (!connection.profiles.audio.types.packet) {
            return
        }
        connection.events.fire(PlaySoundEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Sound event (category=$category, position=$position, sound=$sound, volume=$volume, pitch=$pitch)" }
    }
}
