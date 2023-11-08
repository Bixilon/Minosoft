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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering", "models"])
class CuboidUtilTest {

    private fun uv(u1: Int, v1: Int, u2: Int, v2: Int) = FaceUV(Vec2(u1, v1), Vec2(u2, v2))

    fun `simple uv`() {
        val from = Vec3(-7, 0, -7)
        val to = Vec3(7, 10, 7)
        val offset = Vec2i(0, 19)

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.DOWN), uv(28, 33, 42, 19))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.UP), uv(14, 19, 28, 33))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.NORTH), uv(42, 33, 56, 43))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.SOUTH), uv(14, 33, 28, 43))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.EAST), uv(28, 33, 42, 43))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.WEST), uv(0, 33, 14, 43))
    }

    fun `not same size uv`() {
        val from = Vec3(-1, 0, 0)
        val to = Vec3(1, 4, 1)
        val offset = Vec2i(0, 0)

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.DOWN), uv(3, 1, 5, 0))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.UP), uv(1, 0, 3, 1))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.NORTH), uv(4, 1, 6, 5))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.SOUTH), uv(1, 1, 3, 5))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.EAST), uv(3, 1, 4, 5))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.WEST), uv(0, 1, 1, 5))
    }

    fun `pig head`() {
        val from = Vec3(-4, 8, 6)
        val to = Vec3(4, 16, 14)
        val offset = Vec2i(0, 0)

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.DOWN), uv(16, 8, 24, 0))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.UP), uv(8, 0, 16, 8))

        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.WEST), uv(0, 8, 8, 16))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.SOUTH), uv(8, 8, 16, 16))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.EAST), uv(16, 8, 24, 16))
        assertEquals(CuboidUtil.cubeUV(offset, from, to, Directions.NORTH), uv(24, 8, 32, 16))
    }
}
