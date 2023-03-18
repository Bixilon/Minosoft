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

package de.bixilon.minosoft.data.registries.blocks.light

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.light.DirectedProperty.Companion.isSideCovered
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DirectedPropertyTest {

    @Test
    fun testSideCovered1() {
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.DOWN))
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.UP))
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.NORTH))
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.SOUTH))
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.WEST))
        assertTrue(VoxelShape(AABB.BLOCK).isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered2() {
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.DOWN))
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.UP))
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.NORTH))
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.SOUTH))
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.WEST))
        assertFalse(VoxelShape(AABB.EMPTY).isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered3() {
        val shape = VoxelShape(AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f))
        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertFalse(shape.isSideCovered(Directions.NORTH))
        assertFalse(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered4() {
        val shape = VoxelShape(AABB(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f))
        assertFalse(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertTrue(shape.isSideCovered(Directions.NORTH))
        assertFalse(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered5() {
        val shape = VoxelShape(
            AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f),
            AABB(0.0f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f),
        )

        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertTrue(shape.isSideCovered(Directions.UP))
        assertTrue(shape.isSideCovered(Directions.NORTH))
        assertTrue(shape.isSideCovered(Directions.SOUTH))
        assertTrue(shape.isSideCovered(Directions.WEST))
        assertTrue(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered6() {
        val shape = VoxelShape(
            AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f),
            AABB(0.0f, 0.5f, 0.0f, 1.0f, 1.0f, 0.5f),
        )

        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertTrue(shape.isSideCovered(Directions.NORTH))
        assertFalse(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered7() {
        val shape = VoxelShape(
            AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f),
            AABB(0.0f, 0.5f, 0.5f, 1.0f, 1.0f, 1.0f),
        )

        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertFalse(shape.isSideCovered(Directions.NORTH))
        assertTrue(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }
}
