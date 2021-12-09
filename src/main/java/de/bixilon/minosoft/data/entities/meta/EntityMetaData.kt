/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.meta

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerData
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerLevels
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerTypes
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i
import java.util.*

class EntityMetaData(
    val connection: PlayConnection,
) {
    val sets: MetaDataHashMap = MetaDataHashMap()

    override fun toString(): String {
        return sets.toString()
    }

    fun getData(type: EntityMetaDataDataTypes, buffer: PlayInByteBuffer): Any? {
        return when (type) {
            EntityMetaDataDataTypes.BYTE -> buffer.readByte()
            EntityMetaDataDataTypes.VAR_INT -> buffer.readVarInt()
            EntityMetaDataDataTypes.SHORT -> buffer.readUnsignedShort()
            EntityMetaDataDataTypes.INT -> buffer.readInt()
            EntityMetaDataDataTypes.FLOAT -> buffer.readFloat()
            EntityMetaDataDataTypes.STRING -> buffer.readString()
            EntityMetaDataDataTypes.CHAT -> buffer.readChatComponent()
            EntityMetaDataDataTypes.BOOLEAN -> buffer.readBoolean()
            EntityMetaDataDataTypes.VEC3I -> Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt())
            EntityMetaDataDataTypes.ITEM_STACK -> buffer.readItemStack()
            EntityMetaDataDataTypes.ITEM_STACK -> buffer.readItemStack()
            EntityMetaDataDataTypes.ROTATION -> ArmorStandArmRotation(buffer.readFloat(), buffer.readFloat(), buffer.readFloat())
            EntityMetaDataDataTypes.BLOCK_POSITION -> buffer.readBlockPosition()
            EntityMetaDataDataTypes.OPT_CHAT -> buffer.readOptional { buffer.readChatComponent() }
            EntityMetaDataDataTypes.OPT_BLOCK_POSITION -> buffer.readOptional { buffer.readBlockPosition() }
            EntityMetaDataDataTypes.DIRECTION -> buffer.readDirection()
            EntityMetaDataDataTypes.OPT_UUID -> buffer.readOptional { buffer.readUUID() }
            EntityMetaDataDataTypes.NBT -> buffer.readNBT()
            EntityMetaDataDataTypes.PARTICLE -> buffer.readParticle()
            EntityMetaDataDataTypes.POSE -> buffer.readPose()
            EntityMetaDataDataTypes.BLOCK_ID -> buffer.connection.registries.blockStateRegistry[buffer.readVarInt()] // ToDo
            EntityMetaDataDataTypes.OPT_VAR_INT -> buffer.readVarInt() - 1
            EntityMetaDataDataTypes.VILLAGER_DATA -> VillagerData(VillagerTypes[buffer.readVarInt()], connection.registries.villagerProfessionRegistry[buffer.readVarInt()].resourceLocation, VillagerLevels[buffer.readVarInt()])
            EntityMetaDataDataTypes.OPT_BLOCK_ID -> buffer.connection.registries.blockStateRegistry[buffer.readVarInt()]
        }
    }

    enum class EntityMetaDataDataTypes {
        BYTE,
        SHORT,
        INT,
        VAR_INT,
        FLOAT,
        STRING,
        CHAT,
        OPT_CHAT,
        ITEM_STACK,
        BOOLEAN,
        VEC3I,
        ROTATION,
        BLOCK_POSITION,
        OPT_BLOCK_POSITION,
        DIRECTION,
        OPT_UUID,
        BLOCK_ID,
        OPT_BLOCK_ID,
        NBT,
        PARTICLE,
        VILLAGER_DATA,
        OPT_VAR_INT,
        POSE,
        ;

        companion object : ValuesEnum<EntityMetaDataDataTypes> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, EntityMetaDataDataTypes> = KUtil.getEnumValues(VALUES)
        }
    }

    inner class MetaDataHashMap : HashMap<Int, Any>() {

        operator fun <K> get(field: EntityMetaDataFields): K {
            val index: Int = this@EntityMetaData.connection.registries.getEntityMetaDataIndex(field) ?: return field.defaultValue as K // Can not find field.
            get(index)?.let {
                try {
                    return it as K
                } catch (exception: ClassCastException) {
                    Log.log(LogMessageType.OTHER, level = LogLevels.WARN, message = exception)
                }
            }
            return field.defaultValue as K
        }

        fun getPose(field: EntityMetaDataFields): Poses? {
            return get(field)
        }

        fun getByte(field: EntityMetaDataFields): Byte {
            return get(field) ?: 0
        }

        fun getVillagerData(field: EntityMetaDataFields): VillagerData {
            return get(field)
        }

        fun getParticle(field: EntityMetaDataFields): ParticleData {
            return get(field)
        }

        fun getNBT(field: EntityMetaDataFields): Map<String, Any>? {
            return get(field)
        }

        fun getBlock(field: EntityMetaDataFields): BlockState? {
            return get(field)
        }

        fun getUUID(field: EntityMetaDataFields): UUID? {
            return get(field)
        }

        fun getDirection(field: EntityMetaDataFields): Directions {
            return get(field)
        }

        fun getVec3i(field: EntityMetaDataFields): Vec3i? {
            return get(field)
        }

        fun getBlockPosition(field: EntityMetaDataFields): Vec3i? {
            return get(field)
        }

        fun getRotation(field: EntityMetaDataFields): ArmorStandArmRotation {
            return get(field)
        }

        fun getBoolean(field: EntityMetaDataFields): Boolean {
            val ret: Any = get(field) ?: return false
            if (ret is Byte) {
                return ret == 0x01
            }
            return ret as Boolean
        }

        fun getBitMask(field: EntityMetaDataFields, bitMask: Int): Boolean {
            val byte: Byte = getByte(field)
            return BitByte.isBitMask(byte.toInt(), bitMask)
        }

        fun getItemStack(field: EntityMetaDataFields): ItemStack? {
            return get(field)
        }

        fun getChatComponent(field: EntityMetaDataFields): ChatComponent? {
            return get<Any?>(field)?.let { ChatComponent.of(it, connection.language) }
        }

        fun getString(field: EntityMetaDataFields): String? {
            return get(field)
        }

        fun getFloat(field: EntityMetaDataFields): Float {
            return get(field) ?: 0.0f
        }

        fun getInt(field: EntityMetaDataFields): Int {
            val ret: Any = get(field) ?: 0
            if (ret is Byte) {
                return ret.toInt()
            }
            return ret as Int
        }

        fun getShort(field: EntityMetaDataFields): Short {
            return get(field) ?: 0
        }
    }
}
