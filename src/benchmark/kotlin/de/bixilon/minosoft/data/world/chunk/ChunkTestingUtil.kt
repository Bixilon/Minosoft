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

package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.manager.SimpleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.TestBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import org.objenesis.ObjenesisStd
import org.testng.annotations.Test
import kotlin.reflect.jvm.javaField

const val SECTIONS = 16

object ChunkTestingUtil {
    private val world = createWorld()

    fun createConnection(): PlayConnection {
        val connection = ObjenesisStd().newInstance(PlayConnection::class.java)

        Connection::events.javaField!!.forceSet(connection, EventMaster())
        return connection
    }

    fun createWorld(): World {
        val objenesis = ObjenesisStd()
        val world = objenesis.newInstance(World::class.java)
        world::dimension.javaField!!.forceSet(world, DataObserver(DimensionProperties(skyLight = true)))
        world::connection.javaField!!.forceSet(world, createConnection())

        return world
    }

    fun createEmptyChunk(position: ChunkPosition): Chunk {
        val objenesis = ObjenesisStd()
        val chunk = objenesis.newInstance(Chunk::class.java)
        Chunk::lock.javaField!!.forceSet(chunk, ThreadLock())
        chunk::chunkPosition.forceSet(position)
        Chunk::world.javaField!!.forceSet(chunk, world)
        Chunk::maxSection.javaField!!.forceSet(chunk, chunk.world.dimension.maxSection)
        Chunk::connection.javaField!!.forceSet(chunk, chunk.world.connection)
        Chunk::light.javaField!!.forceSet(chunk, ChunkLight(chunk))
        Chunk::neighbours.javaField!!.forceSet(chunk, ChunkNeighbours(chunk))
        chunk.sections = arrayOfNulls(SECTIONS)

        return chunk
    }

    fun createChunkWithNeighbours(): Chunk {
        val chunk = createEmptyChunk(Vec2i.EMPTY)
        var index = 0
        for (x in -1..1) {
            for (z in -1..1) {
                if (x == 0 && z == 0) {
                    continue
                }
                chunk.neighbours[index++] = createEmptyChunk(Vec2i(x, z))
            }
        }

        return chunk
    }

    fun ChunkSection.fill(state: BlockState) {
        for (index in 0 until 4096) {
            blocks.unsafeSet(index, state)
        }
    }

    fun Chunk.fillBottom(state: BlockState) {
        getOrPut(0, false)!!.fill(state)
    }

    @Test
    fun testChunkTestingUtil() {
        createChunkWithNeighbours()
    }

    fun createBlock(name: String, luminance: Int, lightProperties: LightProperties): Block {
        val block = TestBlock(minosoft(name), BlockSettings())
        val state = AdvancedBlockState(block, properties = emptyMap(), collisionShape = AbstractVoxelShape.EMPTY, outlineShape = AbstractVoxelShape.EMPTY, luminance = luminance, lightProperties = lightProperties, solidRenderer = true)
        block::states.forceSet(SimpleStateManager(state))

        return block
    }

    fun createSolidBlock(): Block {
        return createBlock("solid", 0, OpaqueProperty)
    }

    fun createOpaqueLight(): Block {
        return createBlock("solid_light", 15, OpaqueProperty)
    }
}
