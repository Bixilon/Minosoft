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

package de.bixilon.minosoft.gui.rendering.chunk.mesh.cache

import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["mesher"])
class BlockMesherCacheTest {
    private val ENTITIES by lazy { BlockMesherCache::class.java.getFieldOrNull("entities")!!.field }

    private fun BlockMesherCache.entities(): BlockEntityCacheState? = ENTITIES[this]

    private fun create(): BlockMesherCache {
        val context = RenderContext::class.java.allocate()
        context::queue.forceSet(Queue(1))


        return BlockMesherCache(context)
    }

    fun `entities initially null`() {
        val cache = create()
        assertEquals(cache.entities(), null)
    }


    fun `create renderer once`() {
        val cache = create()
        val entity = TestBlockEntity()

        val renderer = cache.createEntity(InSectionPosition(1, 2, 3), entity)
        assertSame(entity.renderer, renderer)

        assertNotNull(cache.entities())
        assertEquals(entity.creations, 1)
    }

    fun `create renderer twice`() {
        val cache = create()
        val entity = TestBlockEntity()

        cache.createEntity(InSectionPosition(1, 2, 3), entity)
        cache.createEntity(InSectionPosition(1, 2, 3), entity)

        assertNotNull(cache.entities())
        assertEquals(entity.creations, 1)
    }

    fun `create renderer twice with cleanup`() {
        val cache = create()
        val entity = TestBlockEntity()

        cache.createEntity(InSectionPosition(1, 2, 3), entity)
        cache.unmark()
        cache.cleanup()
        cache.createEntity(InSectionPosition(1, 2, 3), entity)

        assertNotNull(cache.entities())
        assertEquals(entity.creations, 2)
    }

    fun `drop all`() {
        val cache = create()
        val entity = TestBlockEntity()

        cache.createEntity(InSectionPosition(1, 2, 3), entity)
        cache.drop()

        cache.context.queue.work()

        assertEquals(entity.status, TestBlockEntity.State.DROPPED)
    }

    fun `unload all`() {
        val cache = create()
        val entity = TestBlockEntity()

        cache.createEntity(InSectionPosition(1, 2, 3), entity)
        cache.unload()

        cache.context.queue.work()

        assertEquals(entity.status, TestBlockEntity.State.UNLOADED)
    }

    fun `unload single unused`() {
        val cache = create()
        val entity = TestBlockEntity()

        cache.createEntity(InSectionPosition(1, 2, 3), entity)
        cache.unmark()
        cache.cleanup()

        cache.context.queue.work()

        assertEquals(entity.status, TestBlockEntity.State.UNLOADED)
    }

    fun `multiple entities`() {
        val cache = create()
        val a = TestBlockEntity()
        val b = TestBlockEntity()

        val aa = cache.createEntity(InSectionPosition(1, 2, 3), a)
        val bb = cache.createEntity(InSectionPosition(1, 2, 4), b)

        assertSame(a.renderer, aa)
        assertSame(b.renderer, bb)
        assertNotSame(aa, bb)
    }

    fun `cleanup just one entity`() {
        val cache = create()
        val a = TestBlockEntity()
        val b = TestBlockEntity()

        val aa = cache.createEntity(InSectionPosition(1, 2, 3), a)
        val bb = cache.createEntity(InSectionPosition(1, 2, 4), b)

        cache.unmark()
        cache.createEntity(InSectionPosition(1, 2, 3), a)
        cache.cleanup()

        cache.context.queue.work()

        assertEquals(a.status, TestBlockEntity.State.LOADED)
        assertEquals(b.status, TestBlockEntity.State.UNLOADED)
    }


    private class TestBlockEntity : BlockEntity(PlaySession::class.java.allocate(), BlockPosition(1, 2, 3), TestBlockStates.ENTITY1) {
        val renderer = Renderer()
        var creations = 0
        var status = State.LOADED

        override fun createRenderer(context: RenderContext): BlockEntityRenderer {
            creations++
            return renderer
        }

        inner class Renderer : BlockEntityRenderer {
            override fun load() = Broken()

            override fun unload() {
                assert(status == State.LOADED)
                status = State.UNLOADED
            }

            override fun drop() {
                assert(status == State.LOADED)
                status = State.DROPPED
            }

            override fun draw() = Broken()

        }

        enum class State {
            LOADED,
            UNLOADED,
            DROPPED,
        }
    }
}
