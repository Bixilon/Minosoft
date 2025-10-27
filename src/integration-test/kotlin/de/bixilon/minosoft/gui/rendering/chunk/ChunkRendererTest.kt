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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kutil.time.TimeUtil.sleep
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.RenderTestUtil
import de.bixilon.minosoft.gui.rendering.RenderTestUtil.frame
import de.bixilon.minosoft.test.IT
import org.testng.Assert
import org.testng.annotations.Test
import kotlin.time.Duration.Companion.milliseconds

@Test(groups = ["chunk_renderer"], dependsOnGroups = ["rendering", "block"])
class ChunkRendererTest {

    private fun create(): ChunkRenderer {
        val renderer = ChunkRenderer(RenderTestUtil.context.session, RenderTestUtil.context)

        return renderer
    }

    private fun ChunkRenderer.awaitQueue(count: Int) {
        for (i in 0 until 200) {
            sleep(16.milliseconds)
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
        val chunk = RenderTestUtil.context.session.world.chunks[ChunkPosition(0, 0)]!!
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, ignoreVisibility = true)
        sleep(50.milliseconds)
        renderer.frame()
        renderer.awaitQueue(0)
        Assert.assertEquals(renderer.loaded.size, 0)
    }

    fun queueSingleChunk() {
        val chunk = RenderTestUtil.context.session.world.chunks[ChunkPosition(0, 0)]!!
        chunk[InChunkPosition(0, 0, 0)] = IT.BLOCK_1
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, ignoreVisibility = true)
        renderer.awaitQueue(1)
        chunk[InChunkPosition(0, 0, 0)] = null // reset
        Assert.assertEquals(renderer.loaded.size, 1)
    }

    @Test(invocationCount = 10)
    fun queueMultipleChunks() {
        val chunks = setOf(
            RenderTestUtil.context.session.world.chunks[ChunkPosition(0, 0)]!!,
            RenderTestUtil.context.session.world.chunks[ChunkPosition(0, 1)]!!,
            RenderTestUtil.context.session.world.chunks[ChunkPosition(1, 1)]!!,
            RenderTestUtil.context.session.world.chunks[ChunkPosition(3, 1)]!!,
        )
        for (chunk in chunks) {
            chunk[InChunkPosition(0, 0, 0)] = IT.BLOCK_1
            chunk[InChunkPosition(0, 16, 0)] = IT.BLOCK_1
        }
        val renderer = create()
        for (chunk in chunks) {
            renderer.master.tryQueue(chunk, ignoreLoaded = true, ignoreVisibility = true)
        }
        renderer.awaitQueue(chunks.size)

        val count = renderer.loaded.size
        // reset
        for (chunk in chunks) {
            chunk[InChunkPosition(0, 0, 0)] = null
            chunk[InChunkPosition(0, 16, 0)] = null
        }
        Assert.assertEquals(count, chunks.size)
    }
}
