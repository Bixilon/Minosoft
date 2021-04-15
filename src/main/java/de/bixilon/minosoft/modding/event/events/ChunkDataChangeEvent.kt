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
package de.bixilon.minosoft.modding.event.events

import de.bixilon.minosoft.data.world.ChunkData

import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketChunkData
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import glm_.vec2.Vec2i

/**
 * Fired when a new chunk is received or a full chunk changes
 */
class ChunkDataChangeEvent : PlayConnectionEvent {
    val chunkPosition: Vec2i
    val chunkData: ChunkData?
    val heightMap: CompoundTag?

    constructor(connection: PlayConnection, position: Vec2i, chunkData: ChunkData, heightMap: CompoundTag?) : super(connection) {
        this.chunkPosition = position
        this.chunkData = chunkData
        this.heightMap = heightMap
    }

    constructor(connection: PlayConnection, position: Vec2i, chunkData: ChunkData) : super(connection) {
        this.chunkPosition = position
        this.chunkData = chunkData
        heightMap = CompoundTag()
    }

    constructor(connection: PlayConnection, pkg: PacketChunkData) : super(connection) {
        chunkPosition = pkg.chunkPosition
        chunkData = pkg.chunkData
        heightMap = pkg.heightMap
    }
}
