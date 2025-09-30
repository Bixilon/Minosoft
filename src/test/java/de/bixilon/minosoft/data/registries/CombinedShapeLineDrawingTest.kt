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

package de.bixilon.minosoft.data.registries

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.CombinedShape
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class CombinedShapeLineDrawingTest {

    @Test
    fun test1() {
        val shape = AABB(Vec3f.EMPTY, Vec3f.ONE)
        assertTrue { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 0, 0)) }
    }

    @Test
    fun test2() {
        val shape = AABB(Vec3f.EMPTY, Vec3f.ONE)
        assertTrue { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(2, 0, 0)) }
    }

    @Test
    fun test3() {
        val shape = AABB(Vec3f.EMPTY, Vec3f.ONE)
        // ToDo assertFalse { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 1, 0)) }
    }

    @Test
    fun test4() {
        val shape = AABB(Vec3f.EMPTY, Vec3f(1f, 0.5f, 1f))
        // ToDo assertFalse { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 0.5, 0)) }
    }

    @Test
    fun test5() {
        val shape = AABB(Vec3f.EMPTY, Vec3f(1f, 0.5f, 1f))
        assertTrue { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 0, 0)) }
    }

    @Test
    fun test6() {
        val shape = CombinedShape(AABB(Vec3f.EMPTY, Vec3f(1f, 0.5f, 1f)), AABB(Vec3f(0f, 0.5f, 0f), Vec3f(1, 1, 1)))
        // assertFalse { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 0, 0)) }
    }

    @Test
    fun test7() {
        val shape = CombinedShape(AABB(Vec3f.EMPTY, Vec3f(1f, 0.5f, 1f)), AABB(Vec3f(0f, 0.5f, 0f), Vec3f(1, 1, 1)))
        // ToDo    assertFalse { shape.shouldDrawLine(Vec3f.EMPTY, Vec3f(1, 1, 0)) }
    }

    @Test
    fun test8() {
        val shape = CombinedShape(AABB(Vec3f.EMPTY, Vec3f(1f, 0.5f, 1f)), AABB(Vec3f(0f, 0.5f, 0f), Vec3f(0.5f, 1f, 1f)))
        // assertFalse { shape.shouldDrawLine(Vec3f(0, 0.5, 0), Vec3f(0, 0.5, 1)) }
    }
}
