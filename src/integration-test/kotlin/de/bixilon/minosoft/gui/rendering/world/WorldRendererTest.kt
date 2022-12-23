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

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.blocks.StoneTestO
import de.bixilon.minosoft.gui.rendering.RenderTestUtil
import de.bixilon.minosoft.gui.rendering.RenderTestUtil.frame
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["world_renderer"], dependsOnGroups = ["rendering", "block"])
class WorldRendererTest {

    private fun create(): WorldRenderer {
        val renderer = WorldRenderer(RenderTestUtil.context.connection, RenderTestUtil.context)

        return renderer
    }

    private fun WorldRenderer.awaitQueue(count: Int) {
        for (i in 0 until 1000) {
            Thread.sleep(10)
            frame()
            if (loaded.size == 1) {
                break
            }
        }
    }

    @Test(priority = -1)
    fun loadModels() {
        val latch = CountUpAndDownLatch(1)
        RenderTestUtil.context.modelLoader.load(latch)
        latch.dec()
        latch.await()
    }

    fun testCreation() {
        Assert.assertNotNull(create())
    }

    fun queueEmptyChunk() {
        val chunk = RenderTestUtil.context.connection.world[Vec2i(0, 0)]!!
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, force = true)
        Thread.sleep(50)
        renderer.frame()
        renderer.awaitQueue(0)
        Assert.assertEquals(renderer.loaded.size, 0)
    }

    fun queueSingleChunk() {
        val chunk = RenderTestUtil.context.connection.world[Vec2i(0, 0)]!!
        chunk[Vec3i(0, 0, 0)] = StoneTestO.state
        val renderer = create()
        renderer.master.tryQueue(chunk, ignoreLoaded = true, force = true)
        renderer.awaitQueue(1)
        chunk[Vec3i(0, 0, 0)] = null
        Assert.assertEquals(renderer.loaded.size, 1)
    }
}
