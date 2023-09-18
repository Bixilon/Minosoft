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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeType
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeContainerUpdate
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeModifier
import de.bixilon.minosoft.datafixer.rls.EntityAttributeFixer.fix
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W08A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class EntityAttributesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int = buffer.readEntityId()
    val attributes: Map<AttributeType, AttributeContainerUpdate>

    init {
        val attributes: MutableMap<AttributeType, AttributeContainerUpdate> = mutableMapOf()
        val attributeCount: Int = if (buffer.versionId < V_21W08A) {
            buffer.readInt()
        } else {
            buffer.readVarInt()
        }
        for (i in 0 until attributeCount) {
            val type = MinecraftAttributes[buffer.readResourceLocation().fix()]
            val baseValue: Double = buffer.readDouble()
            val update = AttributeContainerUpdate(base = baseValue)
            val modifierCount: Int = if (buffer.versionId < V_14W04A) buffer.readUnsignedShort() else buffer.readVarInt()

            for (ii in 0 until modifierCount) {
                val uuid: UUID = buffer.readUUID()
                val amount: Double = buffer.readDouble()
                val operation = AttributeOperations[buffer.readUnsignedByte()]
                update.modifier[uuid] = AttributeModifier(null, uuid, amount, operation)
            }
            if (type == null) {
                continue
            }
            attributes[type] = update
        }
        this.attributes = attributes
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities[entityId]?.nullCast<LivingEntity>()?.attributes?.update(this.attributes)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Entity effect attributes (entityId=$entityId, attributes=$attributes)" }
    }
}
