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

package de.bixilon.minosoft.data.registries

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class VoxelShapeLineDrawingTest {

    @Test
    fun test1() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3.ONE))
        assertTrue { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 0, 0)) }
    }

    @Test
    fun test2() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3.ONE))
        assertTrue { shape.shouldDrawLine(Vec3.EMPTY, Vec3(2, 0, 0)) }
    }

    @Test
    fun test3() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3.ONE))
        // ToDo assertFalse { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 1, 0)) }
    }

    @Test
    fun test4() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3(1, 0.5, 1)))
        // ToDo assertFalse { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 0.5, 0)) }
    }

    @Test
    fun test5() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3(1, 0.5, 1)))
        assertTrue { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 0, 0)) }
    }

    @Test
    fun test6() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3(1, 0.5, 1)), AABB(Vec3(0, 0.5, 0), Vec3(1, 1, 1)))
        // assertFalse { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 0, 0)) }
    }

    @Test
    fun test7() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3(1, 0.5, 1)), AABB(Vec3(0, 0.5, 0), Vec3(1, 1, 1)))
        // ToDo    assertFalse { shape.shouldDrawLine(Vec3.EMPTY, Vec3(1, 1, 0)) }
    }

    @Test
    fun test8() {
        val shape = VoxelShape(AABB(Vec3.EMPTY, Vec3(1, 0.5, 1)), AABB(Vec3(0, 0.5, 0), Vec3(0.5, 1, 1)))
        // assertFalse { shape.shouldDrawLine(Vec3(0, 0.5, 0), Vec3(0, 0.5, 1)) }
    }
}
