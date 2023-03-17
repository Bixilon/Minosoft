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

package de.bixilon.minosoft.data.registries.shapes.voxel

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class VoxelShapeTest {

    @Test
    fun testEquals() {
        val a = VoxelShape(AABB(Vec3(0.0), Vec3(1.0)))
        val b = VoxelShape(AABB(Vec3(0.0), Vec3(1.0)))
        assertEquals(a, b)
    }

    @Test
    fun testNotEquals() {
        val a = VoxelShape(AABB(Vec3(0.1), Vec3(1.0)))
        val b = VoxelShape(AABB(Vec3(0.0), Vec3(1.0)))
        assertNotEquals(a, b)
    }
}
