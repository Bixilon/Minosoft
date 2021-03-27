/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.LevelTypes
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.world.biome.accessor.BlockBiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.modding.event.events.JoinGameEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import de.bixilon.minosoft.util.nbt.tag.ListTag
import de.bixilon.minosoft.util.nbt.tag.NBTTag
import kotlin.experimental.and

class PacketJoinGame : ClientboundPacket() {
    var entityId: Int = 0
    var isHardcore: Boolean = false
    var gamemode: Gamemodes = Gamemodes.SPECTATOR
    var dimension: Dimension? = null
    var difficulty: Difficulties = Difficulties.NORMAL
    var viewDistance = -1
    var maxPlayers = 0
    var levelType: LevelTypes = LevelTypes.DEFAULT
    var isReducedDebugScreen = false
    var isEnableRespawnScreen = true
    var hashedSeed: Long = 0L
    var dimensions: HashBiMap<ResourceLocation, Dimension> = HashBiMap.create()

    override fun read(buffer: InByteBuffer): Boolean {
        entityId = buffer.readInt()

        if (buffer.versionId < V_20W27A) {
            val gamemodeRaw = buffer.readByte()
            isHardcore = BitByte.isBitSet(gamemodeRaw.toLong(), 3)
            // remove hardcore bit and get gamemode
            gamemode = Gamemodes.byId((gamemodeRaw and (0x8.inv())).toInt())
        } else {
            isHardcore = buffer.readBoolean()
            gamemode = Gamemodes.byId(buffer.readUnsignedByte().toInt())
        }

        if (buffer.versionId < ProtocolVersions.V_1_9_1) {
            dimension = buffer.connection.mapping.dimensionRegistry.get(buffer.readByte().toInt())
            difficulty = Difficulties.byId(buffer.readUnsignedByte().toInt())
            maxPlayers = buffer.readByte().toInt()
            if (buffer.versionId >= ProtocolVersions.V_13W42B) {
                levelType = LevelTypes.byType(buffer.readString())
            }
            if (buffer.versionId < ProtocolVersions.V_14W29A) {
                return true
            }
            isReducedDebugScreen = buffer.readBoolean()
        }

        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
            buffer.readByte() // previous game mode
        }
        if (buffer.versionId >= ProtocolVersions.V_20W22A) {
            buffer.readStringArray() // world
        }
        if (buffer.versionId < ProtocolVersions.V_20W21A) {
            dimension = buffer.connection.mapping.dimensionRegistry.get(buffer.readInt())
        } else {
            val dimensionCodec = buffer.readNBT()
            dimensions = parseDimensionCodec(dimensionCodec, buffer.versionId)
            if (buffer.versionId < ProtocolVersions.V_1_16_2_PRE3) {
                dimension = dimensions[buffer.readResourceLocation()]!!
            } else {
                val tag = buffer.readNBT() as CompoundTag
                val parsedDimension = Dimension.deserialize(ResourceLocation(Util.generateRandomString(10)), tag) // ToDo: Why no resource Location?
                for ((_, entry) in dimensions) {
                    if (parsedDimension.bareEquals(entry)) {
                        dimension = entry
                        break
                    }
                }
                check(dimension != null) { "Can not find dimension!" }
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_20W22A) {
            buffer.readString() // world
        }
        if (buffer.versionId >= ProtocolVersions.V_19W36A) {
            hashedSeed = buffer.readLong()
        }
        if (buffer.versionId < ProtocolVersions.V_19W11A) {
            difficulty = Difficulties.byId(buffer.readUnsignedByte().toInt())
        }
        maxPlayers = if (buffer.versionId < ProtocolVersions.V_1_16_2_RC1) {
            buffer.readByte().toInt()
        } else {
            buffer.readVarInt()
        }
        if (buffer.versionId < ProtocolVersions.V_20W20A) {
            levelType = LevelTypes.byType(buffer.readString())
        }
        if (buffer.versionId >= ProtocolVersions.V_19W13A) {
            viewDistance = buffer.readVarInt()
        }
        if (buffer.versionId >= ProtocolVersions.V_20W20A) {
            buffer.readBoolean() // isDebug
            if (buffer.readBoolean()) {
                levelType = LevelTypes.FLAT
            }
        }
        isReducedDebugScreen = buffer.readBoolean()
        if (buffer.versionId >= ProtocolVersions.V_19W36A) {
            isEnableRespawnScreen = buffer.readBoolean()
        }
        return true
    }

    override fun handle(connection: Connection) {
        if (connection.fireEvent(JoinGameEvent(connection, this))) {
            return
        }
        connection.player.gamemode = gamemode
        connection.player.world.isHardcore = isHardcore
        connection.mapping.dimensionRegistry.setData(dimensions)
        connection.player.world.dimension = dimension
        val entity = PlayerEntity(connection, entityId, connection.player.playerUUID, null, null, connection.player.playerName, null, null)
        connection.player.entity = entity
        connection.player.world.addEntity(entity)
        connection.player.world.hashedSeed = hashedSeed
        connection.player.world.biomeAccessor = if (connection.version.versionId < ProtocolVersions.V_19W36A) {
            BlockBiomeAccessor(connection.player.world)
        } else {
            NoiseBiomeAccessor(connection.player.world)
        }
        connection.sender.sendChatMessage("I am alive! ~ Minosoft")
    }

    private fun parseDimensionCodec(nbt: NBTTag, versionId: Int): HashBiMap<ResourceLocation, Dimension> {
        if (nbt !is CompoundTag) {
            throw IllegalArgumentException()
        }
        val dimensionMap: HashBiMap<ResourceLocation, Dimension> = HashBiMap.create()
        val listTag: ListTag = if (versionId < ProtocolVersions.V_20W28A) {
            nbt.getListTag("dimension")
        } else {
            nbt.getCompoundTag("minecraft:dimension_type").getListTag("value")
        }
        for (tag in listTag.getValue<NBTTag>()) {
            check(tag is CompoundTag) { "Invalid dimension codec!" }

            val dimensionResourceLocation = tag.getStringTag(if (versionId < ProtocolVersions.V_1_16_PRE3) {
                "key"
            } else {
                "name"
            }).value
            val dimensionPropertyTag = if (versionId < ProtocolVersions.V_1_16_PRE3 || versionId >= ProtocolVersions.V_1_16_2_PRE1) {
                tag.getCompoundTag("element")
            } else {
                tag
            }
            dimensionMap[ResourceLocation(dimensionResourceLocation)] = Dimension.deserialize(ResourceLocation(dimensionResourceLocation), dimensionPropertyTag)
        }
        return dimensionMap
    }

    override fun log() {
        Log.protocol(String.format("[IN] Receiving join game packet (entityId=%s, gamemode=%s, dimension=%s, difficulty=%s, hardcore=%s, viewDistance=%d)", entityId, gamemode, dimension, difficulty, isHardcore, viewDistance))
    }

    override fun onError(connection: Connection) {
        connection.disconnect()
    }
}
