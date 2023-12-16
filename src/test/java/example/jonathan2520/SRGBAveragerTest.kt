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
package example.jonathan2520

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SRGBAveragerTest {

    @Test
    fun `mix transparent 1`() {
        assertEquals(0xFFFFFFFF.toInt(), SRGBAverager.average(0xFFFFFF00.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
    }

    @Test
    fun `mix transparent 2`() {
        assertEquals(0x00, SRGBAverager.average(0xFFFFFF00.toInt(), 0xFFFFFF00.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
    }

    @Test
    fun `mix transparent 3`() {
        assertEquals(0x00, SRGBAverager.average(0xFFFFFF00.toInt(), 0xFFFFFF00.toInt(), 0xFFFFFF00.toInt(), 0xFFFFFFFF.toInt()))
    }
}
