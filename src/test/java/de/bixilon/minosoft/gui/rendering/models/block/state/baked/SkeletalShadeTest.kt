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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kmath.mat.mat3.f.MMat3f
import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.primitive.FloatUtil.rad
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.asin

class SkeletalShadeTest {
    private val DEGREE_90 = 1.5707964f


    fun interpolateShade(normal: Float, max: Float): Float {
        var delta = normal
        if (delta < 0.0f) delta = -delta
        if (delta <= 0.003f) return 0.0f
        if (delta >= 1.0f) return max
        delta = asin(delta) / DEGREE_90 // asin is just defined in |x| <= 1


        return delta * max
    }

    fun getShade(normal: MVec3f): Float {
        normal.normalizeAssign() // for testing purposes
        // Take code from skeletal/shade.glsl

        val x = interpolateShade(normal.x, 0.6f)
        val y: Float
        y = if (normal.y < 0.0f) {
            interpolateShade(normal.y, 0.5f)
        } else {
            interpolateShade(normal.y, 1.0f)
        }
        val z = interpolateShade(normal.z, 0.8f)

        return x + y + z
    }

    fun transformNormal(normal: Vec3f, transform: Mat4f): Vec3f {
        //  return normalize(mat3(transpose(inverse(transform))) * normal);
        return (MMat3f(transform) * normal).apply { normalizeAssign() }.unsafe
    }

    @Test
    fun up() {
        assertEquals(1.0f, getShade(MVec3f(0, 1, 0)))
    }

    @Test
    fun down() {
        assertEquals(0.5f, getShade(MVec3f(0, -1, 0)))
    }

    @Test
    fun north() {
        assertEquals(0.8f, getShade(MVec3f(0, 0, 1)))
    }

    @Test
    fun south() {
        assertEquals(0.8f, getShade(MVec3f(0, 0, -1)))
    }

    @Test
    fun west() {
        assertEquals(0.6f, getShade(MVec3f(1, 0, 0)))
    }

    @Test
    fun east() {
        assertEquals(0.6f, getShade(MVec3f(-1, 0, 0)))
    }

    @Test
    fun northWest() {
        assertEquals(0.7f, getShade(MVec3f(1, 0, 1)))
    }

    @Test
    fun eastSouth() {
        assertEquals(0.7f, getShade(MVec3f(-1, 0, -1)))
    }

    @Test
    fun westUp() {
        assertEquals(0.8f, getShade(MVec3f(1, 1, 0)))
    }

    @Test
    fun westDown() {
        assertEquals(0.55f, getShade(MVec3f(-1, -1, 0)))
    }

    @Test
    fun westNorthUp() {
        assertEquals(0.94f, getShade(MVec3f(1, 1, 1)))
    }


    private fun assertEquals(actual: Vec3f, expected: Vec3f) {
        val delta = actual - expected
        if (delta.length2() < 0.01f) return
        throw AssertionError("Expected $expected, but got $actual")
    }

    @Test
    fun `transform rotate Y 90deg`() {
        val transform = MMat4f().apply { rotateYAssign(90.0f.rad) }
        val normal = Vec3f(0.0f, 1.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(0.0f, 1.0f, 0.0f))
    }

    @Test
    fun `transform rotate Y 180deg`() {
        val transform = MMat4f().apply { rotateYAssign(180.0f.rad) }
        val normal = Vec3f(0.0f, -1.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(0.0f, -1.0f, 0.0f))
    }


    @Test
    fun `transform rotate Y 90deg 2`() {
        val transform = MMat4f().apply { rotateYAssign(90.0f.rad) }
        val normal = Vec3f(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(0.0f, 0.0f, -1.0f))
    }

    @Test
    fun `transform rotate Y 180deg 2`() {
        val transform = MMat4f().apply { rotateYAssign(180.0f.rad) }
        val normal = Vec3f(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `transform translated`() {
        val transform = MMat4f().apply {
            translateAssign(Vec3f(123, 456, 789))
            rotateYAssign(180.0f.rad)
        }
        val normal = Vec3f(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `transform translated scaled`() {
        val transform = MMat4f().apply {
            scaleAssign(0.4f)
            translateAssign(Vec3f(123, 456, 789))
            rotateYAssign(180.0f.rad)
        }
        val normal = Vec3f(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform.unsafe), Vec3f(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `somehow broken in the shader`() {
        val transform = Mat4f(
            -0.93298566f, 0.0f, 0.09189103f, 0.0f,
            -0.09186335f, 0.023007425f, -0.9327047f, 0.0f,
            -0.0022551212f, -0.9372176f, -0.02289664f, 0.0f,
            -455.2743f, 95.37174f, 618.4536f, 1.0f,
        )
        val expected = Vec3f(1.0f, 0.0f, 0.0f)
        val normal = transformNormal(expected, transform)
        assertEquals(normal, Vec3f(-1.0f, 0.0f, 0.0f))
        assertEquals(getShade(normal.unsafe), 0.6f)
    }


    private fun assertEquals(expected: Float, actual: Float) {
        if (abs(expected - actual) < 0.03f) return
        throw AssertionError("Expected $expected but got $actual")
    }
}
