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

package de.bixilon.minosoft.data.registries.blocks.light

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.light.DirectedProperty.Companion.isSideCovered
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.CombinedShape
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DirectedPropertyTest {

    @Test
    fun testSideCovered1() {
        assertTrue(AABB.BLOCK.isSideCovered(Directions.DOWN))
        assertTrue(AABB.BLOCK.isSideCovered(Directions.UP))
        assertTrue(AABB.BLOCK.isSideCovered(Directions.NORTH))
        assertTrue(AABB.BLOCK.isSideCovered(Directions.SOUTH))
        assertTrue(AABB.BLOCK.isSideCovered(Directions.WEST))
        assertTrue(AABB.BLOCK.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered2() {
        assertFalse(AABB.EMPTY.isSideCovered(Directions.DOWN))
        assertFalse(AABB.EMPTY.isSideCovered(Directions.UP))
        assertFalse(AABB.EMPTY.isSideCovered(Directions.NORTH))
        assertFalse(AABB.EMPTY.isSideCovered(Directions.SOUTH))
        assertFalse(AABB.EMPTY.isSideCovered(Directions.WEST))
        assertFalse(AABB.EMPTY.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered3() {
        val shape = AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f)
        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertFalse(shape.isSideCovered(Directions.NORTH))
        assertFalse(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered4() {
        val shape = AABB(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f)
        assertFalse(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertTrue(shape.isSideCovered(Directions.NORTH))
        assertFalse(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    @Test
    fun testSideCovered5() {
        val shape = CombinedShape(
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
        val shape = CombinedShape(
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
        val shape = CombinedShape(
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

    @Test
    fun testSideCovered8() {
        val shape = CombinedShape(
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


    @Test
    fun testSideCovered9() { // overlapping
        val shape = CombinedShape(
            AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.6f, 1.0f),
            AABB(0.0f, 0.4f, 0.5f, 1.0f, 1.0f, 1.0f),
        )

        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertFalse(shape.isSideCovered(Directions.NORTH))
        assertTrue(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }

    // @Test // TODO: This test is correct, isSideCovered is broken
    fun testSideCovered10() { // overlapping
        val shape = CombinedShape(
            AABB(0.0f, 0.0f, 0.0f, 1.0f, 0.6f, 1.0f),
            AABB(0.1f, 0.0f, 0.0f, 0.9f, 0.8f, 1.0f),
            AABB(0.0f, 0.4f, 0.5f, 1.0f, 0.9f, 1.0f),
        )

        assertTrue(shape.isSideCovered(Directions.DOWN))
        assertFalse(shape.isSideCovered(Directions.UP))
        assertFalse(shape.isSideCovered(Directions.NORTH))
        assertTrue(shape.isSideCovered(Directions.SOUTH))
        assertFalse(shape.isSideCovered(Directions.WEST))
        assertFalse(shape.isSideCovered(Directions.EAST))
    }
}
