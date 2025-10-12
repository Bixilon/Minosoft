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

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.rad
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.rotateAssign
import de.bixilon.minosoft.util.KUtil.rad
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class Vec3fUtilTest {

    @Test
    fun `rotate simple x`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(90.0f.rad, Axes.X, false)

        assertEquals(vec, MVec3f(1.0f, -3.0f, 2.0f))
    }

    @Test
    fun `rotate simple y`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(90.0f.rad, Axes.Y, false)

        assertEquals(vec, MVec3f(-3.0f, 2.0f, 1.0f))
    }

    @Test
    fun `rotate simple z`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(90.0f.rad, Axes.Z, false)

        assertEquals(vec, MVec3f(-2.0f, 1.0f, 3.0f))
    }

    @Test
    fun `rotate simple origin x`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(Vec3f(90, 0, 0).rad, Vec3f(1, 1, 1), false)

        assertEquals(vec, MVec3f(1.0f, -1.0f, 2.0f))
    }

    @Test
    fun `rotate simple origin y`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(Vec3f(0, 90, 0).rad, Vec3f(1, 1, 1), false)

        assertEquals(vec, MVec3f(-1.0f, 2.0f, 1.0f))
    }

    @Test
    fun `rotate simple origin z`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(Vec3f(0, 0, 90).rad, Vec3f(1, 1, 1), false)

        assertEquals(vec, MVec3f(0.0f, 1.0f, 3.0f))
    }

    @Test
    fun `rotate all axes`() {
        val vec = MVec3f(1, 2, 3)
        vec.rotateAssign(Vec3f(90, 90, 90).rad, Vec3f(1, 1, 1), false)

        assertEquals(vec, MVec3f(3.0f, 0.0f, 1.0f))
    }

    @Test
    fun `cow test`() {
        val vec = MVec3f(-0.375f, 1.125f, 1.0f)
        vec.rotateAssign(-Vec3f(-90, 0, 0).rad, Vec3f(0.0f, 1.1875f, 0.5625f), false)

        assertEquals(vec, MVec3f(-0.375f, 0.75f, 0.5f))
    }
}
