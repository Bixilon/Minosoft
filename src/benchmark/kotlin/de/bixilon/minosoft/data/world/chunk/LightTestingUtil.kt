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

package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.state.manager.SingleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.TestBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkSectionManagement
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Versions
import org.objenesis.ObjenesisStd
import org.testng.annotations.Test

const val SECTIONS = 16

object LightTestingUtil {
    private val world = createWorld()

    fun createSession(): PlaySession {
        val session = ObjenesisStd().newInstance(PlaySession::class.java)

        Session::events.field[session] = EventMaster()
        return session
    }

    fun createWorld(): World {
        val objenesis = ObjenesisStd()
        val world = objenesis.newInstance(World::class.java)
        world::dimension.forceSet(DataObserver(DimensionProperties(skyLight = true)))
        world::session.forceSet(createSession())

        return world
    }

    fun createEmptyChunk(position: ChunkPosition): Chunk {
        val objenesis = ObjenesisStd()
        val chunk = objenesis.newInstance(Chunk::class.java)

        chunk::lock.forceSet(ReentrantRWLock())
        chunk::position.forceSet(position.raw)
        chunk::world.forceSet(world)
        chunk::light.forceSet(ChunkLight(chunk))
        chunk::neighbours.forceSet(ChunkNeighbours(chunk))
        chunk::sections.forceSet(ChunkSectionManagement(chunk))

        return chunk
    }

    fun createChunkWithNeighbours(): Chunk {
        val chunk = createEmptyChunk(ChunkPosition.EMPTY)
        for (x in -1..1) {
            for (z in -1..1) {
                val offset = ChunkPosition(x, z)
                if (offset == ChunkPosition.EMPTY) {
                    continue
                }
                chunk.neighbours[offset] = createEmptyChunk(offset)
            }
        }

        return chunk
    }

    fun ChunkSection.fill(state: BlockState) {
        for (index in 0 until ChunkSize.BLOCKS_PER_SECTION) {
            blocks.unsafeSet(InSectionPosition(index), state)
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
        val block = TestBlock(minosoft(name), BlockSettings(Versions.AUTOMATIC))
        val state = BlockState(block, properties = emptyMap(), luminance = luminance, lightProperties = lightProperties, flags = IntInlineEnumSet<BlockStateFlags>() + BlockStateFlags.FULLY_OPAQUE)

        block::states.forceSet(SingleStateManager(state))

        return block
    }

    fun createSolidBlock(): Block {
        return createBlock("solid", 0, OpaqueProperty)
    }

    fun createOpaqueLight(): Block {
        return createBlock("solid_light", 15, OpaqueProperty)
    }
}
