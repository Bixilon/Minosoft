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
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.LevelTypes
import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.nbt.tag.CompoundTag

class PacketRespawn(buffer: PlayInByteBuffer) : PlayClientboundPacket() {
    lateinit var dimension: Dimension
        private set
    var difficulty: Difficulties = Difficulties.NORMAL
        private set
    val gamemode: Gamemodes
    var levelType: LevelTypes = LevelTypes.UNKNOWN
        private set
    var hashedSeed = 0L
        private set
    var isDebug = false
        private set
    var isFlat = false
        private set
    var copyMetaData = false
        private set

    init {
        when {
            buffer.versionId < ProtocolVersions.V_20W21A -> {
                dimension = buffer.connection.mapping.dimensionRegistry.get(if (buffer.versionId < ProtocolVersions.V_1_8_9) { // ToDo: this should be 108 but wiki.vg is wrong. In 1.8 it is an int.
                    buffer.readByte().toInt()
                } else {
                    buffer.readInt()
                })
            }
            buffer.versionId < ProtocolVersions.V_1_16_2_PRE3 -> {
                dimension = buffer.connection.mapping.dimensionRegistry.get(buffer.readResourceLocation())!!
            }
            else -> {
                buffer.readNBT() as CompoundTag // current dimension data
            }
        }
        if (buffer.versionId < ProtocolVersions.V_19W11A) {
            difficulty = Difficulties.byId(buffer.readUnsignedByte().toInt())
        }
        if (buffer.versionId >= ProtocolVersions.V_20W22A) {
            dimension = buffer.connection.mapping.dimensionRegistry.get(buffer.readResourceLocation())!!
        }
        if (buffer.versionId >= ProtocolVersions.V_19W36A) {
            hashedSeed = buffer.readLong()
        }
        gamemode = Gamemodes.byId(buffer.readUnsignedByte().toInt())
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE6) {
            buffer.readByte() // previous game mode
        }
        if (buffer.versionId >= ProtocolVersions.V_13W42B && buffer.versionId < ProtocolVersions.V_20W20A) {
            levelType = LevelTypes.byType(buffer.readString())
        }
        if (buffer.versionId >= ProtocolVersions.V_20W20A) {
            isDebug = buffer.readBoolean()
            isFlat = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_20W18A) {
            copyMetaData = buffer.readBoolean()
        }
    }

    override fun handle(connection: PlayConnection) {
        if (connection.fireEvent(RespawnEvent(connection, this))) {
            return
        }

        // clear all chunks
        connection.world.chunks.clear()
        connection.world.dimension = dimension
        connection.player.isSpawnConfirmed = false
        connection.player.entity.tabListItem.gamemode = gamemode
        connection.renderer?.renderWindow?.worldRenderer?.clearChunkCache()
    }

    override fun log() {
        Log.protocol(String.format("[IN] Respawn packet received (dimension=%s, difficulty=%s, gamemode=%s, levelType=%s)", dimension, difficulty, gamemode, levelType))
    }
}
