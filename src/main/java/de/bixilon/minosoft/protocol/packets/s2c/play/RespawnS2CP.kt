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

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.data.types.GlobalPositionEntityDataType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
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
    var levelType: String? = null
        private set
    var hashedSeed = 0L
        private set
    var isDebug = false
        private set
    var isFlat = false
        private set
    var copyMetaData = false
        private set
    var world: ResourceLocation? = null
        private set
    var lastDeathPosition: GlobalPosition? = null
        private set

    init {
        dimension = when {
            buffer.versionId < ProtocolVersions.V_20W21A -> {
                buffer.connection.registries.dimensionRegistry[if (buffer.versionId < ProtocolVersions.V_1_8_9) { // ToDo: this should be 108 but wiki.vg is wrong. In 1.8 it is an int.
                    buffer.readByte().toInt()
                } else {
                    buffer.readInt()
                }].type
            }
            buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 || buffer.versionId >= ProtocolVersions.V_22W19A -> {
                buffer.connection.registries.dimensionRegistry[buffer.readResourceLocation()]!!.type
            }
            else -> {
                DimensionProperties.deserialize(buffer.readNBT().asJsonObject()) // current dimension data
            }
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
            levelType = buffer.readString()
        }
        if (buffer.versionId >= ProtocolVersions.V_20W20A) {
            isDebug = buffer.readBoolean()
            isFlat = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_20W18A) {
            copyMetaData = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_1_19_PRE2) {
            lastDeathPosition = buffer.readPlayOptional { GlobalPositionEntityDataType.read(this) }
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.util.prepareSpawn()
        connection.player.tabListItem.gamemode = gamemode
        connection.world.dimension = dimension
        connection.state = PlayConnectionStates.SPAWNING
        connection.fireEvent(RespawnEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Respawn (dimension=$dimension, difficulty=$difficulty, gamemode=$gamemode, levelType=$levelType)" }
    }
}
