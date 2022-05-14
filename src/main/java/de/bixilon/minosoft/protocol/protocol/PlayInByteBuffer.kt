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
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.json.JsonUtil.toMutableJsonObject
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.player.properties.textures.PlayerTextures
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.particle.data.BlockParticleData
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.particle.data.ItemParticleData
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W21A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W28B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W43A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_13_2_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_9_1_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W28A
import de.bixilon.minosoft.protocol.protocol.encryption.SignatureData
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap


class PlayInByteBuffer : InByteBuffer {
    val connection: PlayConnection
    val versionId: Int

    constructor(bytes: ByteArray, connection: PlayConnection) : super(bytes) {
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
        return ChatComponent.of(readString(), connection.language, null)
    }

    fun readParticleData(): ParticleData {
        val type = connection.registries.particleTypeRegistry[readVarInt()]
        return readParticleData(type)
    }

    @Deprecated("Should be makde with factories")
    fun readParticleData(type: ParticleType): ParticleData {
        // ToDo: Replace with dynamic particle type calling
        if (this.versionId < V_17W45A) {
            return when (type.resourceLocation.full) {
                "minecraft:iconcrack" -> ItemParticleData.read(this, type)
                "minecraft:blockcrack", "minecraft:blockdust", "minecraft:falling_dust" -> BlockParticleData.read(this, type)
                else -> ParticleData(type)
            }
        }

        return when (type.resourceLocation.full) {
            "minecraft:block", "minecraft:falling_dust" -> BlockParticleData.read(this, type)
            "minecraft:dust" -> DustParticleData.read(this, type)
            "minecraft:item" -> ItemParticleData.read(this, type)
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
            if (!connection.version.flattened) {
                metaData = readUnsignedShort()
            }
            val nbt = readNBTTag(versionId < V_14W28B)?.toMutableJsonObject()
            return ItemStackUtil.of(
                item = connection.registries.itemRegistry[id shl 16 or metaData],
                connection = connection,
                count = count,
                durability = metaData,
                nbt = nbt ?: mutableMapOf(),
            )
        }

        return readOptional {
            ItemStackUtil.of(
                item = connection.registries.itemRegistry[readVarInt()],
                connection = connection,
                count = readUnsignedByte(),
                nbt = readNBT()?.toMutableJsonObject() ?: mutableMapOf(),
            )
        }
    }

    @Deprecated("Use readArray", ReplaceWith("readArray(length) { readItemStack() }"))
    fun readItemStackArray(length: Int = readVarInt()): Array<ItemStack?> {
        return readArray(length) { readItemStack() }
    }

    fun readBiomeArray(): Array<Biome> {
        val length = when {
            versionId >= V_20W28A -> readVarInt()
            versionId >= V_19W36A -> 1024

            else -> 0
        }

        check(length <= this.size) { "Trying to allocate too much memory" }

        val ret: MutableList<Biome> = mutableListOf()
        for (i in 0 until length) {
            val biomeId: Int = if (versionId >= V_20W28A) {
                readVarInt()
            } else {
                readInt()
            }
            ret.add(i, connection.registries.biomeRegistry[biomeId])
        }
        return ret.toTypedArray()
    }

    fun readEntityData(): Int2ObjectOpenHashMap<Any?> {
        val data: Int2ObjectOpenHashMap<Any?> = Int2ObjectOpenHashMap()
        if (versionId < V_15W31A) { // ToDo: This version was 48, but this one does not exist!
            var item = readUnsignedByte()
            while (item != 0x7F) {
                val index = item and 0x1F
                val type = connection.registries.entityDataDataDataTypesRegistry[item and 0xFF shr 5]!!
                data[index] = type.type.read(this)
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
                val type = connection.registries.entityDataDataDataTypesRegistry[id] ?: throw IllegalArgumentException("Can not get entity data type (id=$id)")
                data[index] = type.type.read(this)
                index = readUnsignedByte()
            }
        }
        return data
    }

    fun readIngredient(): Ingredient {
        return Ingredient(readArray { readItemStack() })
    }

    @Deprecated("Use readArray", ReplaceWith("readArray(length) { readIngredient() }"))
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

    fun readEntityIdArray(length: Int = readVarInt()): Array<Int> {
        return readArray(length) { readEntityId() }
    }

    fun readPlayerProperties(): PlayerProperties {
        var textures: PlayerTextures? = null
        for (i in 0 until readVarInt()) {
            val name = readString()
            val value = readString()
            val signature = if (versionId < V_14W21A) {
                readString()
            } else {
                readOptional { readString() }
            }
            when (name) {
                PlayerProperties.TEXTURE_PROPERTIES -> {
                    check(textures == null) { "Textures duplicated" }
                    if (signature == null) {
                        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Server tried to send unsigned texture data, ignoring." }
                        continue
                    }
                    textures = PlayerTextures.of(value, signature)
                }
                else -> Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { "Unknown player property $name: $value" }
            }
        }
        return PlayerProperties(
            textures = textures,
        )
    }

    fun <T> readPlayOptional(reader: PlayInByteBuffer.() -> T): T? {
        return if (readBoolean()) {
            reader(this)
        } else {
            null
        }
    }

    fun readChatMessageSender(): ChatMessageSender {
        return ChatMessageSender(readUUID(), readChatComponent())
    }

    fun readSignatureData(): SignatureData {
        return SignatureData(readLong(), readByteArray())
    }
}
