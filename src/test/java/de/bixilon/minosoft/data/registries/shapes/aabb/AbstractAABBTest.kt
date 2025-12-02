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

package de.bixilon.minosoft.data.registries.shapes.aabb

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AbstractAABBTest {

    @Test
    fun `line intersection 1`() {
        val intersects = AbstractAABB.intersects(0.0, 1.0, 1.0, 2.0)
        assertFalse(intersects)
    }

    @Test
    fun `line intersection 2`() {
        val intersects = AbstractAABB.intersects(0.0, 1.5, 1.0, 2.0)
        assertTrue(intersects)
    }

    @Test
    fun `line intersection 3`() {
        val intersects = AbstractAABB.intersects(0.0, 0.9, 1.0, 2.0)
        assertFalse(intersects)
    }

    @Test
    fun `line intersection 4`() {
        val intersects = AbstractAABB.intersects(5.0, 6.0, 1.0, 2.0)
        assertFalse(intersects)
    }

    @Test
    fun `line intersection 5`() {
        val intersects = AbstractAABB.intersects(5.0, 6.0, 1.0, 5.0)
        assertFalse(intersects)
    }
}
