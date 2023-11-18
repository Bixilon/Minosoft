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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
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
            override fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
                this.char++
                assertEquals(tint, ChatColors.BLUE)
                assertEquals(uvStart, Vec2(0.1, 0.2))
                assertEquals(uvEnd, Vec2(0.6, 0.7))
                assertNull(options)
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)

        assertEquals(1, consumer.char)
    }

    fun verifyComplexSetup() {
        var chars = 0
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
                chars++
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, true, true, false, 1.0f, consumer, null)

        assertEquals(4, chars)
    }

    fun unformatted() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, index: Int) {
                assertEquals(start, Vec2(10.0f, 13.0f)) // top spacing
                assertEquals(end, Vec2(15.0f, 21.0f)) // start + width | start + height
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)
    }

    fun `12px height`() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, index: Int) {
                assertEquals(start, Vec2(10.0f, 12.0f)) // -2 for ascent height difference, +1 for normal spacing, +1 for ascent fixing?
                assertEquals(end, Vec2(15.0f, 24.0f))
            }
        }
        val char = DummyCodePointRenderer(ascent = 10.0f, height = 12.0f)

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.0f, consumer, null)
    }

    fun scaled() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
                assertEquals(start, Vec2(10.0f, 13.5f)) // top spacing
                assertEquals(end, Vec2(17.5f, 25.5f)) // start + width | start + height

                // uv stays the same
                assertEquals(uvStart, Vec2(0.1, 0.2))
                assertEquals(uvEnd, Vec2(0.6, 0.7))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, false, false, 1.5f, consumer, null)
    }

    fun shadow() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, index: Int) {
                if (index == 1) return
                assertEquals(start, Vec2(11.0f, 14.0f))
                assertEquals(end, Vec2(16.0f, 22.0f))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, true, false, false, 1.0f, consumer, null)

        assertEquals(consumer.char, 2)
    }

    fun bold() {
        val consumer = object : DummyGUIVertexConsumer() {
            override fun addChar(start: Vec2, end: Vec2, index: Int) {
                if (index == 0) return
                assertEquals(start, Vec2(10.5f, 13.0f))
                assertEquals(end, Vec2(15.5f, 21.0f))
            }
        }
        val char = DummyCodePointRenderer()

        char.render(Vec2(10.0f, 12.0f), TextRenderProperties(), ChatColors.BLUE, false, true, false, 1.0f, consumer, null)

        assertEquals(consumer.char, 2)
    }

    // TODO: ascent
}
