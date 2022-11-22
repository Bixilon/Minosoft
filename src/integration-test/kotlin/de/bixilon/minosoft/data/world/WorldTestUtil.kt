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

package de.bixilon.minosoft.data.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.collections.CollectionUtil
import de.bixilon.kutil.watcher.DataWatcher
import de.bixilon.minosoft.IT
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.ChunkData
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.view.TEST_WORLD_VIEW
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.forceSet

object WorldTestUtil {


    fun createWorld(connection: PlayConnection): World {
        val world = IT.OBJENESIS.newInstance(World::class.java)
        world::chunks.forceSet(CollectionUtil.lockMapOf())
        world::border.forceSet(WorldBorder())
        world::dimension.forceSet(DataWatcher.watched(DimensionProperties()))
        world::connection.forceSet(connection)
        world.chunkMin = Vec2i.EMPTY
        world.chunkMax = Vec2i.EMPTY
        world.chunkSize = Vec2i.EMPTY
        world::view.forceSet(TEST_WORLD_VIEW)

        return world
    }

    fun World.initialize(size: Int) {
        for (x in -size..size) {
            for (z in -size..size) {
                val chunk = getOrCreateChunk(ChunkPosition(x, z))
                chunk.setData(ChunkData(blocks = arrayOfNulls(16)))
            }
        }
    }

    fun World.fill(start: Vec3i, end: Vec3i, state: BlockState?) {
        for (x in start.x..end.x) {
            for (y in start.y..end.y) {
                for (z in start.z..end.z) {
                    this[Vec3i(x, y, z)] = state
                }
            }
        }
    }
}
