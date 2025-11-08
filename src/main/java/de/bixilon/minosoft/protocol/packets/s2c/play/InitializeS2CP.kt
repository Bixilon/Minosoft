/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.data.types.GlobalPositionEntityDataType
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.difficulty.Difficulties
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.network.session.play.channel.vanila.BrandHandler.sendBrand
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_2_PRE1
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W31A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class InitializeS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int
    val isHardcore: Boolean
    var gamemode: Gamemodes = unsafeNull()
        private set
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
    var portalCooldown = 0
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
            if (buffer.versionId < V_23W31A) {
                gamemode = Gamemodes[buffer.readUnsignedByte()]
            }
        }

        if (buffer.versionId < ProtocolVersions.V_1_9_1) {
            dimension = buffer.session.registries.dimension[buffer.readByte().toInt()].properties
            difficulty = Difficulties[buffer.readUnsignedByte()]
            buffer.readUnsignedByte() // max players
            if (buffer.versionId >= ProtocolVersions.V_13W42B) {
                buffer.readString() // level type
            }
            if (buffer.versionId >= ProtocolVersions.V_14W29A) {
                reducedDebugScreen = buffer.readBoolean()
            }
        } else {
            if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6 && buffer.versionId < V_23W31A) {
                buffer.readByte() // previous game mode
            }
            if (buffer.versionId >= ProtocolVersions.V_20W22A) {
                buffer.readArray { buffer.readResourceLocation() } // list of worlds
            }
            if (buffer.versionId < ProtocolVersions.V_20W21A) {
                dimension = buffer.session.registries.dimension[buffer.readInt()].properties
            } else if (buffer.versionId < V_23W31A) {
                registries = buffer.readNBT().asJsonObject()
                if (buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 || buffer.versionId >= ProtocolVersions.V_22W19A) {
                    dimensionName = buffer.readResourceLocation() // dimension type
                } else {
                    dimension = DimensionProperties.deserialize(null, buffer.readNBT().asJsonObject())
                }
                this.world = buffer.readResourceLocation()
            }

            if (buffer.versionId >= ProtocolVersions.V_19W36A && buffer.versionId < V_23W31A) {
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
            if (buffer.versionId >= ProtocolVersions.V_20W20A && buffer.versionId < V_23W31A) {
                buffer.readBoolean() // isDebug
                buffer.readBoolean() // flat
            }
            reducedDebugScreen = buffer.readBoolean()
            if (buffer.versionId >= ProtocolVersions.V_19W36A) {
                respawnScreen = buffer.readBoolean()
            }
            if (buffer.versionId >= V_1_20_2_PRE1) {
                buffer.readBoolean() // limited crafting
            }
            if (buffer.versionId >= V_23W31A) {
                dimensionName = buffer.readResourceLocation()
                world = buffer.readResourceLocation()
                hashedSeed = buffer.readLong()
                gamemode = Gamemodes[buffer.readUnsignedByte()]
                buffer.readByte() // previous gamemode
                buffer.readBoolean() // isDebug
                buffer.readBoolean() // isFlat
            }
            if (buffer.versionId >= ProtocolVersions.V_1_19_PRE2) {
                lastDeathPosition = buffer.readOptional { GlobalPositionEntityDataType.read(buffer) }
            }
            if (buffer.versionId >= ProtocolVersions.V_1_20_PRE1) {
                portalCooldown = buffer.readVarInt()
            }
        }
    }

    override fun handle(session: PlaySession) {
        session.util.resetWorld()
        session.util.prepareSpawn()
        val playerEntity = session.player
        val previousGamemode = playerEntity.additional.gamemode

        if (previousGamemode != gamemode) {
            playerEntity.additional.gamemode = gamemode
            playerEntity.abilities = gamemode.abilities
        }

        session.world.hardcore = isHardcore

        registries?.let { session.registries.updateNbt(session.version, it) }
        session.world.dimension = dimension ?: session.registries.dimension[dimensionName]?.properties ?: throw NullPointerException("Can not find dimension: $dimensionName")
        session.world.name = world


        session.world.entities.clear(session, local = true)
        session.world.entities.add(entityId, null, playerEntity)
        playerEntity.id = entityId
        session.world.biomes.updateNoise(hashedSeed)
        session.world.border.reset()

        if (session.version < V_1_20_2_PRE1) {
            session.settingsManager.sendClientSettings()
        }
        if (!session.version.hasConfigurationState) {
            session.sendBrand()
        }

        if (session.version >= ProtocolVersions.V_1_19_4) { // TODO: find out version
            session.util.signer.reset()
        }
        session.player.keyManagement.sendSession()

        session.events.fire(DimensionChangeEvent(session))
        session.state = PlaySessionStates.SPAWNING
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Initialize (entityId=$entityId, gamemode=$gamemode, dimension=$dimension, difficulty=$difficulty, hardcore=$isHardcore, viewDistance=$viewDistance)" }
    }
}
