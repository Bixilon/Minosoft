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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.data.types.GlobalPositionEntityDataType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.difficulty.Difficulties
import de.bixilon.minosoft.protocol.PacketErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class InitializeS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int
    val isHardcore: Boolean
    val gamemode: Gamemodes
    var dimensionProperties: DimensionProperties? = null
        private set
    var dimensionType: ResourceLocation? = null
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
    var simulationDistance: Int = -1
        private set
    var registries: JsonObject? = null
        private set
    var lastDeathPosition: GlobalPosition? = null
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
            maxPlayers = buffer.readUnsignedByte()
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
                buffer.readArray { buffer.readResourceLocation() } // list of dimensions
            }
            if (buffer.versionId < ProtocolVersions.V_20W21A) {
                dimensionProperties = buffer.connection.registries.dimensionRegistry[buffer.readInt()].type
            } else {
                registries = buffer.readNBT().asJsonObject()
                if (buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 || buffer.versionId >= ProtocolVersions.V_22W19A) {
                    dimensionType = buffer.readResourceLocation() // dimension type
                } else {
                    dimensionProperties = DimensionProperties.deserialize(buffer.readNBT().asJsonObject())
                }
                buffer.readResourceLocation() // dimension id
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
            if (buffer.versionId >= ProtocolVersions.V_21W40A) {
                simulationDistance = buffer.readVarInt()
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
            if (buffer.versionId >= ProtocolVersions.V_1_19_PRE2) {
                lastDeathPosition = buffer.readPlayOptional { GlobalPositionEntityDataType.read(this) }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.util.prepareSpawn()
        val playerEntity = connection.player
        val previousGamemode = playerEntity.additional.gamemode

        if (previousGamemode != gamemode) {
            playerEntity.additional.gamemode = gamemode
        }

        connection.world.hardcore = isHardcore

        registries?.let { connection.registries.update(it) }
        connection.world.dimension = dimensionProperties ?: connection.registries.dimensionRegistry[dimensionType]?.type ?: throw NullPointerException("Can not find dimension: $dimensionType")

        connection.world.entities.getId(playerEntity)?.let { connection.world.entities.remove(it) } // e.g. bungeecord sends this packet twice
        connection.world.entities.add(entityId, null, playerEntity)
        connection.world.hashedSeed = hashedSeed
        if (connection.version.versionId >= ProtocolVersions.V_19W36A && !connection.profiles.rendering.performance.fastBiomeNoise) {
            connection.world.cacheBiomeAccessor = NoiseBiomeAccessor(connection)
        }
        connection.world.border.reset()

        connection.settingsManager.sendClientSettings()

        connection.state = PlayConnectionStates.SPAWNING
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Initialize (entityId=$entityId, gamemode=$gamemode, dimensionType=$dimensionProperties, difficulty=$difficulty, hardcore=$isHardcore, viewDistance=$viewDistance)" }
    }

    companion object : PacketErrorHandler {

        override fun onError(error: Throwable, connection: Connection) {
            connection.network.disconnect()
        }
    }
}
