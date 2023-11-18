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

package de.bixilon.minosoft.gui.rendering.system.base

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering"])
class RenderOrderTest {

    fun `simple iteration`() {
        val order = RenderOrder(intArrayOf(0, 9, 1, 8, 2, 7, 3, 6))

        val result: MutableList<Int> = mutableListOf()
        order.iterate { position, uv -> result += position; result += uv }

        assertEquals(result, listOf(0, 9, 1, 8, 2, 7, 3, 6))
    }

    fun `reverse iteration`() {
        val order = RenderOrder(intArrayOf(0, 9, 1, 8, 2, 7, 3, 6))

        val result: MutableList<Int> = mutableListOf()
        order.iterateReverse { position, uv -> result += position; result += uv }

        assertEquals(result, listOf(3, 6, 2, 7, 1, 8, 0, 9))
    }
}
