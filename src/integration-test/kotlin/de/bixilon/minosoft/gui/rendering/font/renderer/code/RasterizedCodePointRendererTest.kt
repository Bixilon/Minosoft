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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.dummy.DummyCodePointRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.DummyGUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["font"], priority = -1)
class RasterizedCodePointRendererTest {

    fun verifySimpleSetup() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, texture: Texture?, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
                this.char++
                assertEquals(tint, ChatColors.BLUE)
                assertEquals(uvStart, Vec2f(0.1f, 0.2f))
                assertEquals(uvEnd, Vec2f(0.6f, 0.7f))
                assertNull(options)
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)

        assertEquals(1, consumer.char)
    }

    fun verifyComplexSetup() {
        var chars = 0
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, texture: Texture?, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
                chars++
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, true, true, false, 1.0f, consumer, null)

        assertEquals(4, chars)
    }

    fun unformatted() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, index: Int) {
                assertEquals(start, Vec2f(10.0f, 13.0f)) // top spacing
                assertEquals(end, Vec2f(15.0f, 21.0f)) // start + width | start + height
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)
    }

    fun `12px height`() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, index: Int) {
                assertEquals(start, Vec2f(10.0f, 9.2f)) // whatever
                assertEquals(end, Vec2f(15.0f, 21.2f))
            }
        }
        val char = DummyCodePointRenderer(ascent = 10.0f, height = 12.0f)

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)
    }

    fun scaled() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, texture: Texture?, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
                assertEquals(start, Vec2f(10.0f, 13.5f)) // top spacing
                assertEquals(end, Vec2f(17.5f, 25.5f)) // start + width | start + height

                // uv stays the same
                assertEquals(uvStart, Vec2f(0.1f, 0.2f))
                assertEquals(uvEnd, Vec2f(0.6f, 0.7f))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.5f, consumer, null)
    }

    fun shadow() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, index: Int) {
                if (index == 1) return
                assertEquals(start, Vec2f(11.0f, 14.0f))
                assertEquals(end, Vec2f(16.0f, 22.0f))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, true, false, false, 1.0f, consumer, null)

        assertEquals(consumer.char, 2)
    }

    fun bold() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2f, end: Vec2f, index: Int) {
                if (index == 0) return
                assertEquals(start, Vec2f(10.5f, 13.0f))
                assertEquals(end, Vec2f(15.5f, 21.0f))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2f(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, true, false, 1.0f, consumer, null)

        assertEquals(consumer.char, 2)
    }

    // TODO: ascent
}
