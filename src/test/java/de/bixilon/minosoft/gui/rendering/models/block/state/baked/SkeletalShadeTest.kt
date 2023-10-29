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
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.asin

class SkeletalShadeTest {
    private val DEGREE_90 = 1.5707964f


    fun interpolateShade(delta: Float, max: Float): Float {
        var delta = delta
        if (delta < 0.0f) delta = -delta
        if (delta <= 0.0f) return 0.0f
        if (delta >= 1.0f) return max
        return delta * max
    }

    fun getShade(normal: Vec3): Float {
        normal.normalizeAssign() // for testing purposes
        // Take code from skeletal/shade.glsl

        val aX = asin(normal.x) / DEGREE_90
        val aY = asin(normal.y) / DEGREE_90
        val aZ = asin(normal.z) / DEGREE_90

        val x = interpolateShade(aX, 0.6f)
        val y: Float
        y = if (normal.y < 0.0f) {
            interpolateShade(-aY, 0.5f)
        } else {
            interpolateShade(aY, 1.0f)
        }
        val z = interpolateShade(aZ, 0.8f)

        return x + y + z
    }

    @Test
    fun up() {
        assertEquals(1.0f, getShade(Vec3(0, 1, 0)))
    }

    @Test
    fun down() {
        assertEquals(0.5f, getShade(Vec3(0, -1, 0)))
    }

    @Test
    fun north() {
        assertEquals(0.8f, getShade(Vec3(0, 0, 1)))
    }

    @Test
    fun south() {
        assertEquals(0.8f, getShade(Vec3(0, 0, -1)))
    }

    @Test
    fun west() {
        assertEquals(0.6f, getShade(Vec3(1, 0, 0)))
    }

    @Test
    fun east() {
        assertEquals(0.6f, getShade(Vec3(-1, 0, 0)))
    }

    @Test
    fun northWest() {
        assertEquals(0.7f, getShade(Vec3(1, 0, 1)))
    }

    @Test
    fun eastSouth() {
        assertEquals(0.7f, getShade(Vec3(-1, 0, -1)))
    }

    @Test
    fun westUp() {
        assertEquals(0.8f, getShade(Vec3(1, 1, 0)))
    }

    @Test
    fun westDown() {
        assertEquals(0.55f, getShade(Vec3(-1, -1, 0)))
    }

    @Test
    fun westNorthUp() {
        assertEquals(0.94f, getShade(Vec3(1, 1, 1)))
    }

    private fun assertEquals(expected: Float, actual: Float) {
        if (abs(expected - actual) < 0.03f) return
        throw AssertionError("Expected $expected but got $actual")
    }
}
