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

package de.bixilon.minosoft.gui.rendering.skeletal.mesh

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.primitive.IntUtil.toHex
import org.junit.jupiter.api.Test

class SkeletalMeshUtilTest {

    // Take code from skeletal/shade.glsl
    private fun decodeNormalPart(data: Int): Float {
        if (data < 8) return (data / 8.0f) - 1.0f
        return (data - 8) / 7.0f
    }

    private fun decodeNormal(normal: Int): Vec3 {
        val x = normal and 0x0F
        val y = normal shr 8 and 0x0F
        val z = normal shr 4 and 0x0F
        return Vec3(decodeNormalPart(x), decodeNormalPart(y), decodeNormalPart(z))
    }


    private fun assertEquals(actual: Vec3, expected: Vec3) {
        val delta = expected - actual
        if (delta.length2() < 0.1f) return
        throw AssertionError("Mismatch. Expected $expected, actual $actual")
    }

    private fun assertEquals(actual: Int, expected: Int) {
        if (actual == expected) return
        throw AssertionError("Expected ${expected.toHex()} but got ${actual.toHex()}")
    }

    @Test
    fun `encode max negative normal`() {
        val normal = Vec3(-1, -1, -1)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0x00)
        assertEquals(decodeNormal(encoded), Vec3(-1, -1, -1))
    }

    @Test
    fun `encode max positive normal`() {
        val normal = Vec3(1, 1, 1)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0xFFF)
        assertEquals(decodeNormal(encoded), normal)
    }

    @Test
    fun `correct positive clamping`() {
        val normal = Vec3(2, 2, 2)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0xFFF)
        assertEquals(decodeNormal(encoded), Vec3(1, 1, 1))
    }

    @Test
    fun `correct negative clamping`() {
        val normal = Vec3(-2, -2, -2)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0x00)
        assertEquals(decodeNormal(encoded), Vec3(-1, -1, -1))
    }

    @Test
    fun `zero`() {
        val normal = Vec3(0, 0, 0)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0x888)
        assertEquals(decodeNormal(encoded), normal)
    }

    @Test
    fun `zero dot one`() {
        val normal = Vec3(0.15f, 0.15f, 0.15f)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0x999)
        assertEquals(decodeNormal(encoded), normal)
    }

    @Test
    fun `minus zero dot one`() {
        val normal = Vec3(-0.1f, -0.1f, -0.1f)
        val encoded = SkeletalMeshUtil.encodeNormal(normal)
        assertEquals(encoded, 0x777)
        assertEquals(decodeNormal(encoded), normal)
    }
}
