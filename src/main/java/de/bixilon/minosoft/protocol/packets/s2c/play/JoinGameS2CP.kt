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

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.dimension.Dimension
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode.GamemodeChangeEvent
import de.bixilon.minosoft.data.world.biome.accessor.BlockBiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.modding.channels.DefaultPluginChannels
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.protocol.ErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientSettingsC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.PluginMessageC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import de.bixilon.minosoft.util.task.time.TimeWorker
import de.bixilon.minosoft.util.task.time.TimeWorkerTask

class JoinGameS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int
    val isHardcore: Boolean
    val gamemode: Gamemodes
    var dimensionProperties: DimensionProperties
        private set
    var difficulty: Difficulties = Difficulties.NORMAL
        private set
    var viewDistance = 0
        private set
    var maxPlayers = 0
        private set
    var levelType: String? = null
        private set
    var isReducedDebugScreen = false
        private set
    var isEnableRespawnScreen = true
        private set
    var hashedSeed: Long = 0L
        private set
    var dimensions: HashBiMap<ResourceLocation, Dimension> = HashBiMap.create()
        private set
    var worlds: Array<ResourceLocation>? = null
        private set
    var world: ResourceLocation? = null
        private set

    init {
        entityId = buffer.readInt()

        if (buffer.versionId < V_20W27A) {
            val gamemodeRaw = buffer.readUnsignedByte()
            isHardcore = BitByte.isBitSet(gamemodeRaw, 3)
            // remove hardcore bit and get gamemode
            gamemode = Gamemodes[(gamemodeRaw and (0x8.inv()))]
        } else {
            isHardcore = buffer.readBoolean()
            gamemode = Gamemodes[buffer.readUnsignedByte()]
        }

        if (buffer.versionId < ProtocolVersions.V_1_9_1) {
            dimensionProperties = buffer.connection.registries.dimensionRegistry[buffer.readByte().toInt()].type
            difficulty = Difficulties[buffer.readUnsignedByte()]
            maxPlayers = buffer.readByte().toInt()
            if (buffer.versionId >= ProtocolVersions.V_13W42B) {
                levelType = buffer.readString()
            }
            if (buffer.versionId >= ProtocolVersions.V_14W29A) {
                isReducedDebugScreen = buffer.readBoolean()
            }
        } else {
            if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
                buffer.readByte() // previous game mode
            }
            if (buffer.versionId >= ProtocolVersions.V_20W22A) {
                worlds = buffer.readArray { buffer.readResourceLocation() }
            }
            if (buffer.versionId < ProtocolVersions.V_20W21A) {
                dimensionProperties = buffer.connection.registries.dimensionRegistry[buffer.readInt()].type
            } else {
                val dimensionCodec = buffer.readNBT().asCompound()
                dimensions = parseDimensionCodec(dimensionCodec, buffer.versionId)
                dimensionProperties = if (buffer.versionId < ProtocolVersions.V_1_16_2_PRE3) {
                    dimensions[buffer.readResourceLocation()]!!.type
                } else {
                    DimensionProperties.deserialize(buffer.readNBT().asCompound())
                }
                world = buffer.readResourceLocation()
            }

            if (buffer.versionId >= ProtocolVersions.V_19W36A) {
                hashedSeed = buffer.readLong()
            }
            if (buffer.versionId < ProtocolVersions.V_19W11A) {
                difficulty = Difficulties[buffer.readUnsignedByte()]
            }
            maxPlayers = if (buffer.versionId < ProtocolVersions.V_1_16_2_RC1) {
                buffer.readByte().toInt()
            } else {
                buffer.readVarInt()
            }
            if (buffer.versionId < ProtocolVersions.V_20W20A) {
                levelType = buffer.readString()
            }
            if (buffer.versionId >= ProtocolVersions.V_19W13A) {
                viewDistance = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_20W20A) {
                buffer.readBoolean() // isDebug
                if (buffer.readBoolean()) {
                    levelType = "flat"
                }
            }
            isReducedDebugScreen = buffer.readBoolean()
            if (buffer.versionId >= ProtocolVersions.V_19W36A) {
                isEnableRespawnScreen = buffer.readBoolean()
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        val playerEntity = connection.player
        val previousGamemode = playerEntity.tabListItem.gamemode

        if (previousGamemode != gamemode) {
            playerEntity.tabListItem.gamemode = gamemode

            connection.fireEvent(GamemodeChangeEvent(connection, EventInitiators.SERVER, previousGamemode, gamemode))
        }

        connection.world.hardcore = isHardcore
        connection.registries.dimensionRegistry.setData(dimensions)
        connection.world.dimension = dimensionProperties

        connection.world.entities.add(entityId, null, playerEntity)
        connection.world.hashedSeed = hashedSeed
        connection.world.biomeAccessor = if (connection.version.versionId < ProtocolVersions.V_19W36A) {
            BlockBiomeAccessor(connection.world)
        } else {
            NoiseBiomeAccessor(connection.world)
        }
        TimeWorker.addTask(TimeWorkerTask(150, true) { // ToDo: Temp workaround
            connection.sendPacket(ClientSettingsC2SP(viewDistance = Minosoft.config.config.game.camera.viewDistance))

            val brandName = DefaultRegistries.DEFAULT_PLUGIN_CHANNELS_REGISTRY.forVersion(connection.version)[DefaultPluginChannels.BRAND]!!.resourceLocation
            val buffer = PlayOutByteBuffer(connection)
            buffer.writeString("vanilla") // ToDo: Remove prefix
            connection.sendPacket(PluginMessageC2SP(brandName, buffer.toByteArray()))
        })
        connection.state = PlayConnectionStates.SPAWNING
    }

    private fun parseDimensionCodec(nbt: Map<String, Any>, versionId: Int): HashBiMap<ResourceLocation, Dimension> {
        val dimensionMap: HashBiMap<ResourceLocation, Dimension> = HashBiMap.create()
        val listTag: MutableList<Map<*, *>> = if (versionId < ProtocolVersions.V_20W28A) {
            nbt["dimension"]?.listCast()
        } else {
            nbt["minecraft:dimension_type"]?.compoundCast()?.get("value")?.listCast()
        }!!
        for (tag in listTag) {
            val dimensionResourceLocation = ResourceLocation(tag[if (versionId < ProtocolVersions.V_1_16_PRE3) {
                "key"
            } else {
                "name"
            }].unsafeCast())
            val dimensionPropertyTag = if (versionId < ProtocolVersions.V_1_16_PRE3 || versionId >= ProtocolVersions.V_1_16_2_PRE1) {
                tag["element"].asCompound()
            } else {
                tag.asCompound()
            }
            dimensionMap[dimensionResourceLocation] = Dimension.deserialize(null, dimensionResourceLocation, dimensionPropertyTag)
        }
        return dimensionMap
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Join game (entityId=$entityId, gamemode=$gamemode, dimensionType=$dimensionProperties, difficulty=$difficulty, hardcore=$isHardcore, viewDistance=$viewDistance)" }
    }

    companion object : ErrorHandler {

        override fun onError(connection: Connection) {
            connection.disconnect()
        }
    }
}
