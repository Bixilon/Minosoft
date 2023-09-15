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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.pushRight
import kotlin.test.Test
import kotlin.test.assertContentEquals

class BakingUtilTest {
    val from = Vec3(1, 2, 3)
    val to = Vec3(6, 5, 4)


    @Test
    fun positionsDown() {
        assertContentEquals(
            BakingUtil.positions(Directions.DOWN, from, to),
            floatArrayOf(1.0f, 2.0f, 3.0f, 1.0f, 2.0f, 4.0f, 6.0f, 2.0f, 4.0f, 6.0f, 2.0f, 3.0f)
        )
    }

    @Test
    fun positionsUp() {
        assertContentEquals(
            BakingUtil.positions(Directions.UP, from, to),
            floatArrayOf(1.0f, 5.0f, 3.0f, 6.0f, 5.0f, 3.0f, 6.0f, 5.0f, 4.0f, 1.0f, 5.0f, 4.0f)
        )
    }

    @Test
    fun positionsNorth() {
        assertContentEquals(
            BakingUtil.positions(Directions.NORTH, from, to),
            floatArrayOf(1.0f, 2.0f, 3.0f, 6.0f, 2.0f, 3.0f, 6.0f, 5.0f, 3.0f, 1.0f, 5.0f, 3.0f)
        )
    }

    @Test
    fun positionsSouth() {
        assertContentEquals(
            BakingUtil.positions(Directions.SOUTH, from, to),
            floatArrayOf(1.0f, 2.0f, 4.0f, 1.0f, 5.0f, 4.0f, 6.0f, 5.0f, 4.0f, 6.0f, 2.0f, 4.0f)
        )
    }

    @Test
    fun positionsWest() {
        assertContentEquals(
            BakingUtil.positions(Directions.WEST, from, to),
            floatArrayOf(1.0f, 2.0f, 3.0f, 1.0f, 5.0f, 3.0f, 1.0f, 5.0f, 4.0f, 1.0f, 2.0f, 4.0f)
        )
    }

    @Test
    fun positionsEast() {
        assertContentEquals(
            BakingUtil.positions(Directions.EAST, from, to),
            floatArrayOf(6.0f, 2.0f, 3.0f, 6.0f, 2.0f, 4.0f, 6.0f, 5.0f, 4.0f, 6.0f, 5.0f, 3.0f)
        )
    }

    @Test
    fun pushRight1() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(4f, 5f, 0f, 1f, 2f, 3f)
        assertContentEquals(expected, array.pushRight(2, 1))
    }

    @Test
    fun pushRight2() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(2f, 3f, 4f, 5f, 0f, 1f)
        assertContentEquals(expected, array.pushRight(2, 2))
    }

    @Test
    fun pushRight3() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        assertContentEquals(expected, array.pushRight(2, 3))
    }

    @Test
    fun pushLeft1() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(2f, 3f, 4f, 5f, 0f, 1f)
        assertContentEquals(expected, array.pushRight(2, -1))
    }

    @Test
    fun pushLeft2() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(4f, 5f, 0f, 1f, 2f, 3f)
        assertContentEquals(expected, array.pushRight(2, -2))
    }

    @Test
    fun pushLeft3() {
        val array = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val expected = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        assertContentEquals(expected, array.pushRight(2, -3))
    }
}
