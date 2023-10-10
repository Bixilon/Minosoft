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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TintUtilTest {

    @Test
    fun `calculate white up`() {
        val color = TintUtil.calculateTint(0xFFFFFF, Shades.UP)
        assertEquals(color, 0xFFFFFF)
    }

    @Test
    fun `calculate random color up`() {
        val color = TintUtil.calculateTint(0x123456, Shades.UP)
        assertEquals(color, 0x123456)
    }

    @Test
    fun `calculate white down`() {
        val color = TintUtil.calculateTint(0xFFFFFF, Shades.DOWN)
        assertEquals(color, 0x7F7F7F)
    }

    @Test
    fun `calculate white x`() {
        val color = TintUtil.calculateTint(0xFFFFFF, Shades.X)
        assertEquals(color, 0x999999)
    }

    @Test
    fun `calculate white z`() {
        val color = TintUtil.calculateTint(0xFFFFFF, Shades.Z)
        assertEquals(color, 0xCCCCCC)
    }
}
