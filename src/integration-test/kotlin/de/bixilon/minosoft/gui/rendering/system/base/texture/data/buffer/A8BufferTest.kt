/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["textures"])
class A8BufferTest {

    fun `set rgb and check buffer at 0,0`() {
        val source = A8Buffer(Vec2i(12, 13))
        source.setRGBA(0, 0, 0x11, 0x22, 0x33, 0x44)
        assertEquals(source.data.get(0), 0x44)
        assertEquals(source.data.get(1), 0x00)
    }

    fun `set rgba parts and check buffer at random`() {
        val source = A8Buffer(Vec2i(12, 13))
        source.setRGBA(9, 3, 0x11, 0x22, 0x33, 0x44)
        assertEquals(source.data.get(45 + 0), 0x44)
    }
}
