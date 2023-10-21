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

package de.bixilon.minosoft.gui.rendering.models.util

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering", "models"])
class CuboidUtilTest {

    fun `simple uv`() {
        val from = Vec3(-7, 0, -7)
        val to = Vec3(7, 10, 7)
        val offset = Vec2i(0, 19)

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.DOWN), FaceUV(floatArrayOf(14f, 19f, 28f, 33f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.UP), FaceUV(floatArrayOf(28f, 33f, 42f, 19f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.NORTH), FaceUV(floatArrayOf(42f, 33f, 56f, 43f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.SOUTH), FaceUV(floatArrayOf(14f, 33f, 28f, 43f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.WEST), FaceUV(floatArrayOf(28f, 33f, 42f, 43f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.EAST), FaceUV(floatArrayOf(0f, 33f, 14f, 43f)))
    }

    fun `not same size uv`() {
        val from = Vec3(-1, 0, 0)
        val to = Vec3(1, 4, 1)
        val offset = Vec2i(0, 0)

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.DOWN), FaceUV(floatArrayOf(1f, 0f, 3f, 1f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.UP), FaceUV(floatArrayOf(3f, 1f, 5f, 0f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.NORTH), FaceUV(floatArrayOf(4f, 1f, 6f, 5f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.SOUTH), FaceUV(floatArrayOf(1f, 1f, 3f, 5f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.WEST), FaceUV(floatArrayOf(3f, 1f, 4f, 5f)))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.EAST), FaceUV(floatArrayOf(0f, 1f, 1f, 5f)))
    }
}
