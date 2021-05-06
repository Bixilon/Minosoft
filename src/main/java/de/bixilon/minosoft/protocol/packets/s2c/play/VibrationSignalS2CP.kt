/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i

class VibrationSignalS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val sourcePosition: Vec3i = buffer.readBlockPosition()
    val targetType: ResourceLocation = buffer.readResourceLocation()

    /**
     * @return Depends on vibration target type, if block: block postion, if entity: entity id
     */
    val targetData: Any = when (targetType.full) {
        "minecraft:block" -> buffer.readBlockPosition()
        "minecraft:entity" -> buffer.readEntityId()
        else -> error("Unknown target type: $targetType")
    }
    val arrivalTicks: Int = buffer.readVarInt()

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Vibration signal (sourcePosition=$sourcePosition, targetType=$targetType, targetData=$targetData, arrivalTicks=$arrivalTicks)" }
    }
}
