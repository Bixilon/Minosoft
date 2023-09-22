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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.effect

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W06B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_18_2_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_9_4
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W12A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityEffectS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int = buffer.readEntityId()
    var effect: StatusEffectInstance = buffer.readStatusEffectInstance()
    val isAmbient: Boolean
    val hideParticles: Boolean
    val showIcon: Boolean
    var factorCalculationData: JsonObject? = null
        private set


    init {
        var isAmbient = true
        var hideParticles = true
        var showIcon = true
        if (buffer.versionId >= V_14W04A) {
            if (buffer.versionId in V_14W06B until V_1_9_4) { // ToDo
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
        if (buffer.versionId >= V_22W12A) {
            factorCalculationData = buffer.readOptional { buffer.readNBT().toJsonObject() }
        }
    }

    private fun PlayInByteBuffer.readStatusEffectInstance(): StatusEffectInstance {
        val effectId = if (versionId < V_1_18_2_PRE1) {
            readUnsignedByte()
        } else {
            readVarInt()
        }
        val amplifier = readUnsignedByte()
        val duration = if (versionId < V_14W04A) {
            readUnsignedShort()
        } else {
            readVarInt()
        }
        return StatusEffectInstance(connection.registries.statusEffect[effectId], amplifier, duration)
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.entities[entityId]?.nullCast<LivingEntity>() ?: return // thanks mojang
        entity.effects += effect
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Entity effect (entityId=$entityId, effect=$effect, isAmbient=$isAmbient, hideParticles=$hideParticles, showIcon=$showIcon)" }
    }
}
