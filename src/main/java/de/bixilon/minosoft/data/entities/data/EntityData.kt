/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.data

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.decoration.armorstand.ArmorStandArmRotation
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerData
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*

class EntityData(
    val connection: PlayConnection,
) {
    val lock = SimpleLock()
    val sets: EntityDataHashMap = EntityDataHashMap()

    override fun toString(): String {
        return sets.toString()
    }

    inner class EntityDataHashMap : Int2ObjectOpenHashMap<Any>() {

        inline operator fun <reified K> get(field: EntityDataFields): K {
            lock.acquire()
            try {
                val index: Int = this@EntityData.connection.registries.getEntityMetaDataIndex(field) ?: return field.defaultValue.unsafeCast() // Can not find field.
                get(index)?.let {
                    try {
                        return it as K
                    } catch (exception: ClassCastException) {
                        Log.log(LogMessageType.OTHER, level = LogLevels.WARN, message = exception)
                    }
                }
                return field.defaultValue as K
            } finally {
                lock.release()
            }
        }

        fun getPose(field: EntityDataFields): Poses? {
            return get(field)
        }

        fun getByte(field: EntityDataFields): Byte {
            return get(field) ?: 0
        }

        fun getVillagerData(field: EntityDataFields): VillagerData {
            return get(field)
        }

        fun getParticle(field: EntityDataFields): ParticleData {
            return get(field)
        }

        fun getNBT(field: EntityDataFields): Map<String, Any>? {
            return get(field)
        }

        fun getBlock(field: EntityDataFields): BlockState? {
            return get(field)
        }

        fun getUUID(field: EntityDataFields): UUID? {
            return get(field)
        }

        fun getDirection(field: EntityDataFields): Directions {
            return get(field)
        }

        fun getVec3i(field: EntityDataFields): Vec3i? {
            return get(field)
        }

        fun getBlockPosition(field: EntityDataFields): Vec3i? {
            return get(field)
        }

        fun getRotation(field: EntityDataFields): ArmorStandArmRotation {
            return get(field)
        }

        fun getBoolean(field: EntityDataFields): Boolean {
            val ret: Any = get(field) ?: return false
            if (ret is Byte) {
                return ret == 0x01
            }
            return ret as Boolean
        }

        fun getBitMask(field: EntityDataFields, bitMask: Int): Boolean {
            val byte: Byte = getByte(field)
            return BitByte.isBitMask(byte.toInt(), bitMask)
        }

        fun getItemStack(field: EntityDataFields): ItemStack? {
            return get(field)
        }

        fun getChatComponent(field: EntityDataFields): ChatComponent? {
            return get<Any?>(field)?.let { ChatComponent.of(it, connection.language) }
        }

        fun getString(field: EntityDataFields): String? {
            return get(field)
        }

        fun getFloat(field: EntityDataFields): Float {
            return get(field) ?: 0.0f
        }

        fun getInt(field: EntityDataFields): Int {
            val ret: Any = get(field) ?: 0
            if (ret is Byte) {
                return ret.toInt()
            }
            return ret as Int
        }

        fun getShort(field: EntityDataFields): Short {
            return get(field) ?: 0
        }
    }
}
