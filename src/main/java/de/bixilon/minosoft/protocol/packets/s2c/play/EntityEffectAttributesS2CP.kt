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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttribute
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributeModifier
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectOperations
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W08A
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class EntityEffectAttributesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    val attributes: Map<ResourceLocation, EntityAttribute>

    init {
        val attributes: MutableMap<ResourceLocation, EntityAttribute> = mutableMapOf()
        val attributeCount: Int = if (buffer.versionId < V_21W08A) {
            buffer.readInt()
        } else {
            buffer.readVarInt()
        }
        for (i in 0 until attributeCount) {
            val key: ResourceLocation = buffer.readResourceLocation()
            val baseValue: Double = buffer.readDouble()
            val attribute = EntityAttribute(baseValue = baseValue)
            val modifierCount: Int = if (buffer.versionId < V_14W04A) {
                buffer.readUnsignedShort()
            } else {
                buffer.readVarInt()
            }
            for (ii in 0 until modifierCount) {
                val uuid: UUID = buffer.readUUID()
                val amount: Double = buffer.readDouble()
                val operation = StatusEffectOperations[buffer.readUnsignedByte()]
                attribute.modifiers[uuid] = EntityAttributeModifier(key.toString(), uuid, amount, operation)
            }
            attributes[key] = attribute
        }
        this.attributes = attributes.toMap()
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities[entityId]?.let {
            for ((key, attribute) in this.attributes) {
                it.attributes.getOrPut(key) { EntityAttribute(baseValue = it.entityType.attributes[key] ?: 1.0) }.merge(attribute)

            }
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Entity effect attributes (entityId=$entityId, attributes=$attributes)" }
    }
}
