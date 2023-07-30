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

package de.bixilon.minosoft.data.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.getIndex
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.manager.ChunkManager
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.entities.WorldEntities
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.view.TEST_WORLD_VIEW
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.test.IT

object WorldTestUtil {


    fun createWorld(connection: PlayConnection?, light: Boolean = false, capacity: Int = 0): World {
        val world = IT.OBJENESIS.newInstance(World::class.java)
        world::occlusion.forceSet(DataObserver(0))
        world::lock.forceSet(SimpleLock())
        world::chunks.forceSet(ChunkManager(world, maxOf(0, capacity), 0))
        world::border.forceSet(WorldBorder())
        world::dimension.forceSet(DataObserver(DimensionProperties(light = light, skyLight = light)))
        world::connection.forceSet(connection)
        world::entities.forceSet(WorldEntities())
        world::view.forceSet(TEST_WORLD_VIEW)
        world::time.forceSet(DataObserver(WorldTime()))

        return world
    }

    fun World.initialize(size: Int) {
        for (x in -size..size) {
            for (z in -size..size) {
                chunks.create(ChunkPosition(x, z))
            }
        }
    }

    fun World.fill(startX: Int, startY: Int, startZ: Int, endX: Int, endY: Int, endZ: Int, state: BlockState?, superUnsafe: Boolean = true) {
        fill(Vec3i(startX, startY, startZ), Vec3i(endX, endY, endZ), state, superUnsafe)
    }

    fun World.fill(start: Vec3i, end: Vec3i, state: BlockState?, superUnsafe: Boolean = true) {
        // TODO: this can be optimized more (loop chunk after chunk, not x->z)
        var chunk: Chunk? = null
        for (x in start.x..end.x) {
            for (z in start.z..end.z) {
                val chunkPosition = ChunkPosition(x shr 4, z shr 4)
                if (chunk == null) {
                    chunk = this.chunks[chunkPosition] ?: continue
                } else if (chunk.chunkPosition != chunkPosition) {
                    chunk = chunk.neighbours.trace(chunkPosition - chunk.chunkPosition) ?: continue
                }
                for (y in start.y..end.y) {
                    val section = chunk.getOrPut(y.sectionHeight) ?: continue
                    if (superUnsafe) {
                        var data = DATA[section.blocks] as Array<Any?>?
                        if (data == null) {
                            data = arrayOfNulls<Any?>(ProtocolDefinition.BLOCKS_PER_SECTION)
                            DATA[section.blocks] = data
                        }
                        data[getIndex(x and 0x0F, y and 0x0F, z and 0x0F)] = state
                    } else {
                        section.blocks.unsafeSet(x and 0x0F, y and 0x0F, z and 0x0F, state)
                    }
                }
            }
        }

        if (!dimension.light && !dimension.skyLight) return // no need for occlusion when light is ignored

        if (superUnsafe) {
            for (x in (start.x shr 4)..(end.x shr 4)) {
                for (z in (start.z shr 4)..(end.z shr 4)) {
                    val chunkPosition = Vec2i(x, z)
                    chunk = if (chunk != null) {
                        chunk.neighbours.trace(chunkPosition - chunk.chunkPosition) ?: continue
                    } else {
                        this.chunks[chunkPosition] ?: continue
                    }
                    for (sectionHeight in (start.y shr 4)..(end.y shr 4)) {
                        val section = chunk[sectionHeight] ?: continue
                        section.blocks.recalculate(true)
                    }
                }
            }
        }
        recalculateLight(heightmap = true) // yah, might break the result, don't use fill if you want to test light
    }

    private val DATA = SectionDataProvider::class.java.getDeclaredField("data").apply { isAccessible = true }
}
