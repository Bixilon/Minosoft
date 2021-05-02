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
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.particle.data.BlockParticleData
import de.bixilon.minosoft.data.mappings.particle.data.DustParticleData
import de.bixilon.minosoft.data.mappings.particle.data.ItemParticleData
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.mappings.recipes.Ingredient
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.ChatComponent.Companion.of
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import glm_.vec3.Vec3i


class PlayInByteBuffer : InByteBuffer {
    val connection: PlayConnection
    val versionId: Int

    constructor(bytes: ByteArray, connection: PlayConnection) : super(bytes, connection) {
        this.connection = connection
        versionId = connection.version.versionId
    }

    constructor(buffer: PlayInByteBuffer) : super(buffer) {
        connection = buffer.connection
        versionId = connection.version.versionId
    }

    fun readByteArray(): ByteArray {
        val length: Int = if (versionId < V_14W21A) {
            readUnsignedShort()
        } else {
            readVarInt()
        }
        return super.readByteArray(length)
    }


    fun readBlockPosition(): Vec3i {
        // ToDo: protocol id 7
        val raw = readLong()
        val x = raw shr 38
        val y: Long
        val z: Long
        if (versionId < V_18W43A) {
            y = raw shr 26 and 0xFFF
            z = raw shl 38 shr 38
        } else {
            y = raw shl 52 shr 52
            z = raw shl 26 shr 38
        }
        return Vec3i(x, y, z)
    }

    override fun readChatComponent(): ChatComponent {
        return of(readString(), connection.version.localeManager, null)
    }

    fun readParticle(): ParticleData {
        val type = connection.mapping.particleRegistry.get(readVarInt())
        return readParticleData(type)
    }

    fun readParticleData(type: Particle): ParticleData {
        // ToDo: Replace with dynamic particle type calling
        if (this.versionId < V_17W45A) {
            return when (type.resourceLocation.full) {
                "minecraft:iconcrack" -> ItemParticleData(ItemStack(item = connection.mapping.itemRegistry.get(readVarInt() shl 16 or readVarInt()), connection.version), type)
                "minecraft:blockcrack", "minecraft:blockdust", "minecraft:falling_dust" -> BlockParticleData(connection.mapping.getBlockState(readVarInt() shl 4), type) // ToDo: What about meta data?
                else -> ParticleData(type)
            }
        }

        return when (type.resourceLocation.full) {
            "minecraft:block", "minecraft:falling_dust" -> BlockParticleData(connection.mapping.getBlockState(readVarInt()), type)
            "minecraft:dust" -> DustParticleData(readFloat(), readFloat(), readFloat(), readFloat(), type)
            "minecraft:item" -> ItemParticleData(readItemStack(), type)
            else -> ParticleData(type)
        }
    }

    fun readNBT(): Any? {
        return readNBTTag(versionId < V_14W28B)
    }

    fun readItemStack(): ItemStack? {
        if (versionId < V_1_13_2_PRE1) {
            val id = readShort().toInt()
            if (id == -1) {
                return null
            }
            val count = readUnsignedByte()
            var metaData = 0
            if (connection.version.isFlattened()) {
                metaData = readUnsignedShort()
            }
            val nbt = readNBTTag(versionId < V_14W28B)?.compoundCast()
            return ItemStack(
                item = connection.mapping.itemRegistry.get(id shl 16 or metaData),
                version = connection.version,
                count = count,
                durability = metaData,
                nbt = nbt ?: mutableMapOf(),
            )
        }

        return if (readBoolean()) {
            ItemStack(
                version = connection.version,
                item = connection.mapping.itemRegistry.get(readVarInt()),
                count = readUnsignedByte(),
                nbt = readNBT()?.compoundCast() ?: mutableMapOf(),
            )
        } else {
            null
        }
    }

    fun readItemStackArray(length: Int = readVarInt()): Array<ItemStack?> {
        return readArray(length) { readItemStack() }
    }

    fun readBiomeArray(): Array<Biome> {
        val length = when {
            versionId >= V_20W28A -> {
                readVarInt()
            }
            versionId >= V_19W36A -> {
                1024
            }
            else -> {
                0
            }
        }

        check(length <= ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) { "Trying to allocate to much memory" }

        val ret: MutableList<Biome> = mutableListOf()
        for (i in 0 until length) {
            val biomeId: Int = if (versionId >= V_20W28A) {
                readVarInt()
            } else {
                readInt()
            }
            ret.add(i, connection.mapping.biomeRegistry.get(biomeId))
        }
        return ret.toTypedArray()
    }

    fun readMetaData(): EntityMetaData {
        val metaData = EntityMetaData(connection)
        val sets = metaData.sets
        if (versionId < V_15W31A) { // ToDo: This version was 48, but this one does not exist!
            var item = readUnsignedByte()
            while (item != 0x7F) {
                val index = item and 0x1F
                val type = connection.mapping.entityMetaDataDataDataTypesRegistry.get(item and 0xFF shr 5)!!
                sets[index] = metaData.getData(type, this)!!
                item = readUnsignedByte()
            }
        } else {
            var index = readUnsignedByte()
            while (index != 0xFF) {
                val id: Int = if (versionId < V_1_9_1_PRE1) {
                    readUnsignedByte()
                } else {
                    readVarInt()
                }
                val type = connection.mapping.entityMetaDataDataDataTypesRegistry.get(id) ?: error("Can not get meta data index for id $id")
                metaData.getData(type, this)?.let {
                    sets[index] = it
                }
                index = readUnsignedByte()
            }
        }
        return metaData
    }

    fun readIngredient(): Ingredient {
        return Ingredient(readItemStackArray())
    }

    @JvmOverloads
    fun readIngredientArray(length: Int = readVarInt()): Array<Ingredient> {
        return readArray(length) { readIngredient() }
    }

    fun readEntityId(): Int {
        return if (versionId < V_14W04A) {
            readInt()
        } else {
            readVarInt()
        }
    }


}
