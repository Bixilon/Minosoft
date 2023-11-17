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
package de.bixilon.minosoft.protocol.protocol.buffers.play

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toMutableJsonObject
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.chat.signature.ChatSignatureProperties
import de.bixilon.minosoft.data.chat.signature.MessageHeader
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.PlayerTextures
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.particle.data.*
import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.data.registries.registries.registry.EnumRegistry
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.datafixer.rls.ResourceLocationFixer
import de.bixilon.minosoft.protocol.PlayerPublicKey
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.PlayedSound
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W21A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W28B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W43A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_13_2_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_9_1_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W31A
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.time.Instant
import java.util.*


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
        val string = readString()
        if (DebugOptions.LOG_RAW_CHAT) {
            Log.log(LogMessageType.CHAT_IN, LogLevels.VERBOSE) { TextComponent(string) }
        }
        return ChatComponent.of(string, connection.language, null, restricted = true)
    }

    fun readNbtChatComponent(): ChatComponent {
        if (versionId < ProtocolVersions.V_23W40A) {
            return readChatComponent()
        }
        val nbt = readNBT()
        return ChatComponent.of(nbt, connection.language, null, restricted = true)
    }

    fun readParticleData(): ParticleData {
        val type = connection.registries.particleType[readVarInt()]
        return readParticleData(type)
    }

    @Deprecated("Should be made with factories")
    fun readParticleData(type: ParticleType): ParticleData {
        // ToDo: Replace with dynamic particle type calling
        if (this.versionId < V_17W45A) {
            return when (type.identifier.toString()) {
                "minecraft:iconcrack" -> ItemParticleData.read(this, type)
                "minecraft:blockcrack", "minecraft:blockdust", "minecraft:falling_dust" -> BlockParticleData.read(this, type)
                else -> ParticleData(type)
            }
        }

        return when (type.identifier.toString()) {
            "minecraft:block", "minecraft:falling_dust", "minecraft:block_marker" -> BlockParticleData.read(this, type)
            "minecraft:dust" -> DustParticleData.read(this, type)
            "minecraft:item" -> ItemParticleData.read(this, type)
            "minecraft:shriek" -> ShriekParticleData.read(this, type)
            "minecraft:sculk_charge" -> SculkChargeParticleData.read(this, type)
            "minecraft:vibration" -> VibrationParticleData.read(this, type)
            else -> ParticleData(type)
        }
    }

    fun readNBT(): Any? {
        return readNBTTag(versionId < V_14W28B, versionId < V_23W31A)
    }

    fun readItemStack(): ItemStack? {
        if (versionId < V_1_13_2_PRE1) {
            val id = readShort().toInt()
            if (id <= ProtocolDefinition.AIR_BLOCK_ID) {
                return null
            }
            val count = readUnsignedByte()
            var meta = 0
            if (!connection.version.flattened) {
                meta = readUnsignedShort()
            }
            val nbt = readNBT()?.toMutableJsonObject()
            val item = connection.registries.item.getOrNull(id shl 16 or meta) ?: return null // TODO: only if item is not an ItemWithMeta
            return ItemStackUtil.of(
                item = item,
                connection = connection,
                count = count,
                meta = meta,
                nbt = nbt ?: mutableMapOf(),
            )
        }

        return readOptional {
            ItemStackUtil.of(
                item = connection.registries.item[readVarInt()],
                connection = connection,
                count = readUnsignedByte(),
                nbt = readNBT()?.toMutableJsonObject() ?: mutableMapOf(),
            )
        }
    }

    fun readEntityData(): Int2ObjectOpenHashMap<Any?> {
        val data: Int2ObjectOpenHashMap<Any?> = Int2ObjectOpenHashMap()
        if (versionId < V_15W31A) { // ToDo: This version was 48, but this one does not exist!
            var item = readUnsignedByte()
            while (item != 0x7F) {
                val index = item and 0x1F
                val type = connection.registries.entityDataTypes[item and 0xFF shr 5]!!
                data[index] = type.type.read(this)
                item = readUnsignedByte()
            }
            return data
        }
        var index = readUnsignedByte()
        while (index != 0xFF) {
            val id: Int = if (versionId < V_1_9_1_PRE1) readUnsignedByte() else readVarInt()
            val type = connection.registries.entityDataTypes[id] ?: throw IllegalArgumentException("Can not get entity data type (id=$id)")
            data[index] = type.type.read(this)
            index = readUnsignedByte()
        }
        return data
    }

    fun readIngredient(): Ingredient {
        return Ingredient(readArray { readItemStack() })
    }

    fun readEntityId(): Int {
        return if (versionId < V_14W04A) {
            readInt()
        } else {
            readVarInt()
        }
    }

    fun readEntityIdArray(length: Int = readVarInt()): IntArray {
        val array = IntArray(length)
        for (i in array.indices) {
            array[i] = readEntityId()
        }
        return array
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
                        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Server tried to send unsigned texture data, ignoring." }
                        continue
                    }
                    textures = PlayerTextures.of(value, signature)
                }
            }
        }
        return PlayerProperties(
            textures = textures,
        )
    }

    fun readInstant(): Instant {
        val time = readLong()
        if (versionId >= ProtocolVersions.V_22W19A) {
            return Instant.ofEpochMilli(time)
        }
        return Instant.ofEpochSecond(time)
    }

    fun readBitSet(): BitSet {
        return if (versionId < ProtocolVersions.V_20W49A) {
            KUtil.bitSetOf(readVarLong())
        } else {
            BitSet.valueOf(readLongArray())
        }
    }

    fun readBitSet(size: Int): BitSet {
        val bytes = ByteArray(-Math.floorDiv(-size, Byte.SIZE_BITS))
        readByteArray(bytes)
        return BitSet.valueOf(bytes)
    }

    fun <T> readRegistryItem(registry: AbstractRegistry<T>): T {
        return registry[readVarInt()]
    }

    fun <T : RegistryItem> readLegacyRegistryItem(registry: Registry<T>, fixer: ResourceLocationFixer? = null): T? {
        var name = readResourceLocation()
        fixer?.fix(name)?.let { name = it }
        return registry[name]
    }

    fun <T : Enum<*>> readEnum(registry: EnumRegistry<T>): T? {
        return registry[readVarInt()]
    }

    fun readSignatureData(): ByteArray {
        if (versionId < ProtocolVersions.V_22W42A) {
            return readByteArray()
        }
        return readByteArray(ChatSignatureProperties.SIGNATURE_SIZE)
    }

    fun readPlayerPublicKey(): PlayerPublicKey? {
        if (versionId <= ProtocolVersions.V_22W18A) { // ToDo: find version
            return readNBT()?.let { PlayerPublicKey(it.asJsonObject()) }
        }
        if (versionId > ProtocolVersions.V_22W42A) {
            val uuid = readUUID()
        }
        if (versionId < ProtocolVersions.V_22W43A) {
            if (!readBoolean()) {
                return null
            }
        }
        return PlayerPublicKey(readInstant(), CryptManager.getPlayerPublicKey(readByteArray()), readByteArray())
    }

    fun readMessageHeader(): MessageHeader {
        return MessageHeader(readOptional { readByteArray() }, readUUID())
    }

    inline fun <reified T : Enum<T>> readEnumSet(enum: ValuesEnum<T>): Set<T> {
        val bitset = readBitSet(enum.VALUES.size)
        if (bitset.isEmpty) {
            return emptySet()
        }
        val set = enum.set()
        readEnumSet(bitset, set, enum.VALUES)
        return set
    }

    fun <T : Enum<T>> readEnumSet(bitset: BitSet, set: MutableSet<T>, values: Array<T>) {
        for (index in 0 until minOf(bitset.length(), values.size)) {
            if (!bitset.get(index)) {
                continue
            }
            set += values[index]
        }
    }

    inline fun <reified T : Enum<T>> readEnumSet(universe: ValuesEnum<T>, values: Array<T>): Set<T> {
        val bitset = readBitSet(values.size)
        if (bitset.isEmpty) {
            return emptySet()
        }
        val set = universe.set()
        readEnumSet(bitset, set, values)
        return set
    }

    fun readChatMessageParameters(parameters: MutableMap<ChatParameter, ChatComponent>) {
        parameters[ChatParameter.SENDER] = readNbtChatComponent()
        readOptional { readNbtChatComponent() }?.let { parameters[ChatParameter.TARGET] = it }
    }

    fun readSoundPitch(): Float {
        return if (versionId < ProtocolVersions.V_16W20A) {
            readByte() * ProtocolDefinition.SOUND_PITCH_DIVIDER / 100.0f
        } else {
            readFloat()
        }
    }

    override fun readVec3d(): Vec3d {
        if (versionId < ProtocolVersions.V_16W06A) {
            return Vec3d(readFixedPointNumberInt(), readFixedPointNumberInt(), readFixedPointNumberInt())
        }
        return super.readVec3d()
    }

    fun readPositionDelta(): Vec3d {
        if (versionId < ProtocolVersions.V_16W06A) {
            return Vec3d(readFixedPointNumberByte(), readFixedPointNumberByte(), readFixedPointNumberByte())
        }
        return Vec3d(readShort() / 4096.0, readShort() / 4096.0, readShort() / 4096.0) // / 128 / 32
    }

    fun readVelocity(): Vec3d {
        return Vec3d(readShort(), readShort(), readShort()) / ProtocolDefinition.VELOCITY_NETWORK_DIVIDER
    }

    fun readVibrationSource(): Any {
        val type = readResourceLocation()
        val source: Any = when (type.toString()) { // TODO: dynamic, factories
            "minecraft:block" -> readBlockPosition()
            "minecraft:entity" -> readEntityId()
            else -> error("Unknown vibration source: $type")
        }

        return source
    }

    fun readLegacyBitSet(bytes: Int): BitSet {
        val array = ByteArray(bytes)
        for (index in (array.size - 1) downTo 0) { // java BitSet.valueOf is little endian
            array[index] = readByte()
        }
        return BitSet.valueOf(array)
    }

    fun readSound(): PlayedSound {
        if (versionId < ProtocolVersions.V_1_19_3_RC1) {
            return PlayedSound(readRegistryItem(connection.registries.soundEvent))
        }
        val id = readVarInt()
        if (id != 0) {
            return PlayedSound(connection.registries.soundEvent[id - 1])
        }
        val name = readResourceLocation() // TODO: readRegistryItem?
        val attenuation = readOptional { readFloat() }
        return PlayedSound(name, attenuation)
    }
}
