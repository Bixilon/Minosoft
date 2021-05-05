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

import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityStatusEffectS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    var effect: StatusEffectInstance = if (buffer.versionId < ProtocolVersions.V_14W04A) {
        StatusEffectInstance(buffer.connection.mapping.statusEffectRegistry.get(buffer.readUnsignedByte()), buffer.readByte() + 1, buffer.readUnsignedShort())
    } else {
        StatusEffectInstance(buffer.connection.mapping.statusEffectRegistry.get(buffer.readUnsignedByte()), buffer.readByte() + 1, buffer.readVarInt())
    }
    val isAmbient: Boolean
    val hideParticles: Boolean
    val showIcon: Boolean


    init {
        var isAmbient = true
        var hideParticles = true
        var showIcon = true
        if (buffer.versionId >= ProtocolVersions.V_14W04A) {
            if (buffer.versionId < ProtocolVersions.V_1_9_4 && buffer.versionId >= ProtocolVersions.V_14W06B) { // ToDo
                hideParticles = buffer.readBoolean()
            } else {
                val flags = buffer.readByte()
                isAmbient = isBitMask(flags.toInt(), 0x01)
                hideParticles = !isBitMask(flags.toInt(), 0x02)
                if (buffer.versionId >= ProtocolVersions.V_1_14_4) { // ToDo
                    showIcon = isBitMask(flags.toInt(), 0x04)
                }
            }
        }
        this.isAmbient = isAmbient
        this.hideParticles = hideParticles
        this.showIcon = showIcon
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.entities[entityId] ?: return // thanks mojang
        entity.addEffect(effect)
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Entity status effect (entityId=$entityId, effect=$effect, isAmbient=$isAmbient, hideParticles=$hideParticles, showIcon=$showIcon)" }
    }
}
