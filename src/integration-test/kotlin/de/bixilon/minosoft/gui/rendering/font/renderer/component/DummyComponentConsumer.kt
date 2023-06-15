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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.gui.atlas.TexturePart
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderIdentifiable
import org.testng.Assert.assertEquals

class DummyComponentConsumer : GUIVertexConsumer {
    val chars: MutableList<RendererdCodePoint> = mutableListOf()
    val quads: MutableList<RendererdQuad> = mutableListOf()

    override val order: Array<Pair<Int, Int>> get() = emptyArray()
    override fun addVertex(position: Vec2, texture: ShaderIdentifiable?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) = Broken()
    override fun addCache(cache: GUIMeshCache) = Broken()
    override fun ensureSize(size: Int) = Unit

    override fun addQuad(start: Vec2, end: Vec2, texture: TexturePart, tint: RGBColor, options: GUIVertexOptions?) {
        quads += RendererdQuad(Vec2(start), Vec2(end))
    }

    data class RendererdCodePoint(val start: Vec2)
    data class RendererdQuad(val start: Vec2, val end: Vec2)


    inner class ConsumerCodePointRenderer(val width: Float) : CodePointRenderer {
        override fun calculateWidth(scale: Float, shadow: Boolean): Float {
            return width * scale
        }

        override fun render(position: Vec2, properties: TextRenderProperties, color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
            chars += RendererdCodePoint(Vec2(position))
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

    fun assert(vararg chars: RendererdCodePoint) {
        assertEquals(this.chars, chars.toList())
    }

    fun assert(vararg chars: RendererdQuad) {
        assertEquals(this.quads, chars.toList())
    }
}
