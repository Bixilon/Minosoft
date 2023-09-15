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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.gui.rendering.RenderTestUtil
import de.bixilon.minosoft.gui.rendering.RenderTestUtil.frame
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["chunk_renderer"], dependsOnGroups = ["rendering", "block"])
class ChunkRendererTest {

    private fun create(): ChunkRenderer {
        val renderer = ChunkRenderer(RenderTestUtil.context.connection, RenderTestUtil.context)

        return renderer
    }

    private fun ChunkRenderer.awaitQueue(count: Int) {
        for (i in 0 until 200) {
            Thread.sleep(16)
            frame()
            if (loaded.size == count) {
                break
            }
        }
    }

    fun testCreation() {
        Assert.assertNotNull(create())
    }

    fun queueEmptyChunk() {
        val chunk = RenderTestUtil.context.connection.world.chunks[Vec2i(0, 0)]!!
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, force = true)
        Thread.sleep(50)
        renderer.frame()
        renderer.awaitQueue(0)
        Assert.assertEquals(renderer.loaded.size, 0)
    }

    fun queueSingleChunk() {
        val chunk = RenderTestUtil.context.connection.world.chunks[Vec2i(0, 0)]!!
        chunk[Vec3i(0, 0, 0)] = StoneTest0.state
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, force = true)
        renderer.awaitQueue(1)
        chunk[Vec3i(0, 0, 0)] = null // reset
        Assert.assertEquals(renderer.loaded.size, 1)
    }

    @Test(invocationCount = 10)
    fun queueMultipleChunks() {
        val chunks = setOf(
            RenderTestUtil.context.connection.world.chunks[Vec2i(0, 0)]!!,
            RenderTestUtil.context.connection.world.chunks[Vec2i(0, 1)]!!,
            RenderTestUtil.context.connection.world.chunks[Vec2i(1, 1)]!!,
            RenderTestUtil.context.connection.world.chunks[Vec2i(3, 1)]!!,
        )
        for (chunk in chunks) {
            chunk[Vec3i(0, 0, 0)] = StoneTest0.state
            chunk[Vec3i(0, 16, 0)] = StoneTest0.state
        }
        val renderer = create()
        for (chunk in chunks) {
            renderer.master.tryQueue(chunk, ignoreLoaded = true, force = true)
        }
        renderer.awaitQueue(chunks.size)

        val count = renderer.loaded.size
        // reset
        for (chunk in chunks) {
            chunk[Vec3i(0, 0, 0)] = null
            chunk[Vec3i(0, 16, 0)] = null
        }
        Assert.assertEquals(count, chunks.size)
    }
}
