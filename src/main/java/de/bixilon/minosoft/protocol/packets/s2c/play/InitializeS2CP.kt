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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.data.types.GlobalPositionEntityDataType
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.difficulty.Difficulties
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.PacketErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.network.connection.play.channel.vanila.BrandHandler.sendBrand
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class InitializeS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int
    val isHardcore: Boolean
    val gamemode: Gamemodes
    var world: ResourceLocation? = null
        private set
    var dimension: DimensionProperties? = null
        private set
    var dimensionName: ResourceLocation? = null
        private set
    var difficulty: Difficulties = Difficulties.NORMAL
        private set
    var viewDistance = 0
        private set
    var reducedDebugScreen = false
        private set
    var respawnScreen = true
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
            isHardcore = gamemodeRaw.isBit(3)
            // remove hardcore bit and get gamemode
            gamemode = Gamemodes[(gamemodeRaw and (0x8.inv()))]
        } else {
            isHardcore = buffer.readBoolean()
            gamemode = Gamemodes[buffer.readUnsignedByte()]
        }

        if (buffer.versionId < ProtocolVersions.V_1_9_1) {
            dimension = buffer.connection.registries.dimension[buffer.readByte().toInt()].properties
            difficulty = Difficulties[buffer.readUnsignedByte()]
            buffer.readUnsignedByte() // max players
            if (buffer.versionId >= ProtocolVersions.V_13W42B) {
                buffer.readString() // level type
            }
            if (buffer.versionId >= ProtocolVersions.V_14W29A) {
                reducedDebugScreen = buffer.readBoolean()
            }
        } else {
            if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
                buffer.readByte() // previous game mode
            }
            if (buffer.versionId >= ProtocolVersions.V_20W22A) {
                buffer.readArray { buffer.readResourceLocation() } // list of worlds
            }
            if (buffer.versionId < ProtocolVersions.V_20W21A) {
                dimension = buffer.connection.registries.dimension[buffer.readInt()].properties
            } else {
                registries = buffer.readNBT().asJsonObject()
                if (buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 || buffer.versionId >= ProtocolVersions.V_22W19A) {
                    dimensionName = buffer.readResourceLocation() // dimension type
                } else {
                    dimension = DimensionProperties.deserialize(null, buffer.readNBT().asJsonObject())
                }
                this.world = buffer.readResourceLocation()
            }

            if (buffer.versionId >= ProtocolVersions.V_19W36A) {
                hashedSeed = buffer.readLong()
            }
            if (buffer.versionId < ProtocolVersions.V_19W11A) {
                difficulty = Difficulties[buffer.readUnsignedByte()]
            }
            if (buffer.versionId < ProtocolVersions.V_1_16_2_RC1) {
                buffer.readUnsignedByte() // max players
            } else {
                buffer.readVarInt() // max players
            }
            if (buffer.versionId < ProtocolVersions.V_20W20A) {
                buffer.readString() // level type
            }
            if (buffer.versionId >= ProtocolVersions.V_19W13A) {
                viewDistance = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_21W40A) {
                simulationDistance = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_20W20A) {
                buffer.readBoolean() // isDebug
                buffer.readBoolean() // flat
            }
            reducedDebugScreen = buffer.readBoolean()
            if (buffer.versionId >= ProtocolVersions.V_19W36A) {
                respawnScreen = buffer.readBoolean()
            }
            if (buffer.versionId >= ProtocolVersions.V_1_19_PRE2) {
                lastDeathPosition = buffer.readOptional { GlobalPositionEntityDataType.read(buffer) }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.util.resetWorld()
        connection.util.prepareSpawn()
        val playerEntity = connection.player
        val previousGamemode = playerEntity.additional.gamemode

        if (previousGamemode != gamemode) {
            playerEntity.additional.gamemode = gamemode
        }

        connection.world.hardcore = isHardcore

        registries?.let { connection.registries.update(it) }
        connection.world.dimension = dimension ?: connection.registries.dimension[dimensionName]?.properties ?: throw NullPointerException("Can not find dimension: $dimensionName")
        connection.world.name = world


        connection.world.entities.clear(connection, local = true)
        connection.world.entities.add(entityId, null, playerEntity)
        connection.events.fire(EntitySpawnEvent(connection, playerEntity))
        if (connection.version.versionId >= ProtocolVersions.V_19W36A && !connection.profiles.rendering.performance.fastBiomeNoise) {
            connection.world.cacheBiomeAccessor = NoiseBiomeAccessor(connection, hashedSeed)
        }
        connection.world.border.reset()

        connection.settingsManager.sendClientSettings()
        connection.sendBrand()

        if (connection.version >= ProtocolVersions.V_1_19_4) { // TODO: find out version
            connection.util.signer.reset()
        }
        connection.player.keyManagement.sendSession()

        connection.events.fire(DimensionChangeEvent(connection))
        connection.state = PlayConnectionStates.SPAWNING
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Initialize (entityId=$entityId, gamemode=$gamemode, dimension=$dimension, difficulty=$difficulty, hardcore=$isHardcore, viewDistance=$viewDistance)" }
    }

    companion object : PacketErrorHandler {

        override fun onError(error: Throwable, connection: Connection) {
            connection.error = error
            connection.network.disconnect()
        }
    }
}
