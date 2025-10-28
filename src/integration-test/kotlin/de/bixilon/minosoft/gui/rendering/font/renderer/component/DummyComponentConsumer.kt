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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.GuiMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.CharVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import org.testng.Assert.assertEquals

class DummyComponentConsumer : GuiVertexConsumer {
    val chars: MutableList<RenderedCodePoint> = mutableListOf()
    val quads: MutableList<RenderedQuad> = mutableListOf()

    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
    override fun addCache(cache: GuiMeshCache) = Broken()
    override fun ensureSize(primitives: Int) = Unit

    override fun addQuad(start: Vec2f, end: Vec2f, texture: ShaderTexture?, uvStart: Vec2f, uvEnd: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) {
        quads += RenderedQuad(Vec2f(start.unsafe), Vec2f(end.unsafe)) // copy because unsafe
    }

    data class RenderedCodePoint(val start: Vec2f)
    data class RenderedQuad(val start: Vec2f, val end: Vec2f)

    override fun addIndexQuad(front: Boolean, reverse: Boolean) = Unit


    inner class ConsumerCodePointRenderer(val width: Float) : CodePointRenderer {
        override fun calculateWidth(scale: Float, shadow: Boolean): Float {
            return width * scale
        }

        override fun render(position: Vec2f, properties: TextRenderProperties, color: RGBAColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: CharVertexConsumer, options: GUIVertexOptions?) {
            chars += RenderedCodePoint(Vec2f(position.x, position.y)) // copy because unsafe
        }
    }


    inner class Font : FontType {
        private val chars: Array<ConsumerCodePointRenderer?> = arrayOfNulls(26) // a-z

        // a:0 b:0.5 c:1.0 d:1.5 e:2.0 f:2.5 g:3.0 h:3.5

        init {
            build()
        }

        fun build() {
            for (i in 0 until chars.size) {
                chars[i] = ConsumerCodePointRenderer(width = i / 2.0f)
            }
        }

        override fun get(codePoint: Int): CodePointRenderer? {
            if (codePoint in 'a'.code..'z'.code) {
                return chars[codePoint - 'a'.code]
            }
            return null
        }
    }

    fun assert(vararg chars: RenderedCodePoint) {
        assertEquals(this.chars, chars.toList())
    }

    fun assert(vararg chars: RenderedQuad) {
        assertEquals(this.quads, chars.toList())
    }
}
