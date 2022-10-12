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
package de.bixilon.minosoft.modding.event.events.blocks.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

/**
 * Fired when you place or remove a light source in a chunk.
 * Also fired for neighbour chunks, but @param blockChange is false there
 */
class LightChangeEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    val chunkPosition: Vec2i,
    val chunk: Chunk,
    val sectionHeight: Int,
    val blockChange: Boolean,
) : PlayConnectionEvent(connection, initiator)
