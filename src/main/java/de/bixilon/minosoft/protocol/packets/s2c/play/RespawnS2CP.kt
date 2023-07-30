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

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.data.types.GlobalPositionEntityDataType
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.difficulty.Difficulties
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class RespawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    var dimension: DimensionProperties
        private set
    var difficulty: Difficulties = Difficulties.NORMAL
        private set
    val gamemode: Gamemodes
    var hashedSeed = 0L
        private set
    var keepAttributes = false
        private set
    var keepFlags: Byte = 0xFF.toByte()
        private set
    var world: ResourceLocation? = null
        private set
    var lastDeathPosition: GlobalPosition? = null
        private set
    var portalCooldown = 0
        private set

    init {
        dimension = when {
            buffer.versionId < ProtocolVersions.V_20W21A -> {
                val id = if (buffer.versionId < ProtocolVersions.V_1_8_9) { // ToDo: this should be 108 but wiki.vg is wrong. In 1.8 it is an int.
                    buffer.readByte().toInt()
                } else {
                    buffer.readInt()
                }
                buffer.connection.registries.dimension[id].properties
            }

            buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 || buffer.versionId >= ProtocolVersions.V_22W19A -> {
                buffer.readLegacyRegistryItem(buffer.connection.registries.dimension)!!.properties
            }

            else -> DimensionProperties.deserialize(null, buffer.readNBT().asJsonObject())
        }
        if (buffer.versionId < ProtocolVersions.V_19W11A) {
            difficulty = Difficulties[buffer.readUnsignedByte()]
        }
        if (buffer.versionId >= ProtocolVersions.V_20W22A) {
            world = buffer.readResourceLocation()
        }
        if (buffer.versionId >= ProtocolVersions.V_19W36A) {
            hashedSeed = buffer.readLong()
        }
        gamemode = Gamemodes[buffer.readUnsignedByte()]
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
            buffer.readByte() // previous game mode
        }
        if (buffer.versionId >= ProtocolVersions.V_13W42B && buffer.versionId < ProtocolVersions.V_20W20A) {
            buffer.readString() // level type
        }
        if (buffer.versionId >= ProtocolVersions.V_20W20A) {
            buffer.readBoolean() // debug
            buffer.readBoolean() // flat
        }
        if (buffer.versionId >= ProtocolVersions.V_20W18A) {
            if (buffer.versionId >= ProtocolVersions.V_1_19_3_RC3) {
                keepFlags = buffer.readByte()
            } else {
                keepAttributes = buffer.readBoolean()
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_1_19_PRE2) {
            lastDeathPosition = buffer.readOptional { GlobalPositionEntityDataType.read(buffer) }
        }
        if (buffer.versionId >= ProtocolVersions.V_1_20_PRE1) {
            portalCooldown = buffer.readVarInt()
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.util.prepareSpawn()
        connection.player.additional.gamemode = gamemode
        val dimensionChange = this.dimension != connection.world.dimension || this.world != connection.world.name
        if (dimensionChange) {
            connection.util.resetWorld()
        }
        connection.world.dimension = dimension
        connection.world.name = world
        connection.world.cacheBiomeAccessor?.hashedSeed = hashedSeed

        connection.state = PlayConnectionStates.SPAWNING
        if (dimensionChange) {
            connection.events.fire(DimensionChangeEvent(connection))
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Respawn (dimension=$dimension, difficulty=$difficulty, gamemode=$gamemode)" }
    }
}
