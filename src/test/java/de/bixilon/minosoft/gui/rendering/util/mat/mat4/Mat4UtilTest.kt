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

package de.bixilon.minosoft.gui.rendering.util.mat.mat4

import glm_.func.rad
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateXAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateYAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateZAssign
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Mat4UtilTest {

    @Test
    fun `custom rotateX`() {
        val expected = Mat4f().rotateAssign(12.0f.rad, Vec3f(1, 0, 0))
        val actual = Mat4f().apply { Mat4Util.rotateX(this, 12.0f.rad) }

        assertEquals(expected, actual)
    }

    @Test
    fun `custom rotateY`() {
        val expected = Mat4f().rotateAssign(12.0f.rad, Vec3f(0, 1, 0))
        val actual = Mat4f().apply { Mat4Util.rotateY(this, 12.0f.rad) }

        assertEquals(expected, actual)
    }

    @Test
    fun `custom rotateZ`() {
        val expected = Mat4f().rotateAssign(12.0f.rad, Vec3f(0, 0, 1))
        val actual = Mat4f().apply { Mat4Util.rotateZ(this, 12.0f.rad) }

        assertEquals(expected, actual)
    }

    @Test
    fun `custom translateXAssign`() {
        val expected = Mat4f().translateAssign(Vec3f(123.0f, 0, 0))
        val actual = Mat4f().translateXAssign(123.0f)

        assertEquals(expected, actual)
    }

    @Test
    fun `custom translateYAssign`() {
        val expected = Mat4f().translateAssign(Vec3f(0, 123.0f, 0))
        val actual = Mat4f().translateYAssign(123.0f)

        assertEquals(expected, actual)
    }

    @Test
    fun `custom translateZAssign`() {
        val expected = Mat4f().translateAssign(Vec3f(0, 0, 123.0f))
        val actual = Mat4f().translateZAssign(123.0f)

        assertEquals(expected, actual)
    }
}
