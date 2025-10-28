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

package de.bixilon.minosoft.gui.rendering.gui.mesh.consumer

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray

interface CachedGuiVertexConsumer : GuiVertexConsumer {
    val data: FloatList
    val halfSize: Vec2f
    val white: ShaderTexture

    override fun addQuad(positions: FloatArray, uv: PackedUVArray, texture: ShaderTexture, tint: RGBAColor, options: GUIVertexOptions?) {
        val color = options.finalTint(tint)

        addVertex(positions[0 * Vec2f.LENGTH + 0] / halfSize.x - 1.0f, 1.0f - positions[0 * Vec2f.LENGTH + 1] / halfSize.y, uv[0], texture, color)
        addVertex(positions[1 * Vec2f.LENGTH + 0] / halfSize.x - 1.0f, 1.0f - positions[1 * Vec2f.LENGTH + 1] / halfSize.y, uv[1], texture, color)
        addVertex(positions[2 * Vec2f.LENGTH + 0] / halfSize.x - 1.0f, 1.0f - positions[2 * Vec2f.LENGTH + 1] / halfSize.y, uv[2], texture, color)
        addVertex(positions[3 * Vec2f.LENGTH + 0] / halfSize.x - 1.0f, 1.0f - positions[3 * Vec2f.LENGTH + 1] / halfSize.y, uv[3], texture, color)
    }

    override fun addQuad(startX: Float, startY: Float, endX: Float, endY: Float, texture: ShaderTexture, uvStartX: Float, uvStartY: Float, uvEndX: Float, uvEndY: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        val startX = startX / halfSize.x - 1.0f
        val startY = 1.0f - startY / halfSize.y

        val endX = endX / halfSize.x - 1.0f
        val endY = 1.0f - endY / halfSize.y

        val uvStart = texture.transformUV(uvStartX, uvStartY)
        val uvEnd = texture.transformUV(uvEndX, uvEndY)


        val color = options.finalTint(tint)


        addVertex(startX, startY, uvStart, texture, color)
        addVertex(startX, endY, PackedUV(uvStart.u, uvEnd.v), texture, color)
        addVertex(endX, endY, uvEnd, texture, color)
        addVertex(endX, startY, PackedUV(uvEnd.u, uvStart.v), texture, color)
    }

    override fun addQuad(start: Vec2f, end: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) {
        addQuad(start.x, start.y, end.x, end.y, white, 0.0f, 0.0f, 0.0f, 0.0f, tint, options)
    }

    override fun addChar(start: Vec2f, end: Vec2f, texture: ShaderTexture, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
        var topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * FormattingProperties.ITALIC_OFFSET else 0.0f

        topOffset /= halfSize.x

        val startX = start.x / halfSize.x - 1.0f
        val startY = 1.0f - start.y / halfSize.y

        val endX = end.x / halfSize.x - 1.0f
        val endY = 1.0f - end.y / halfSize.y



        addVertex(startX + topOffset, startY, PackedUV(uvStart), texture, tint)
        addVertex(startX, endY, PackedUV(uvStart.x, uvEnd.y), texture, tint)
        addVertex(endX, endY, PackedUV(uvEnd), texture, tint)
        addVertex(endX + topOffset, startY, PackedUV(uvEnd.x, uvStart.y), texture, tint)
    }

    fun add(cache: CachedGuiVertexConsumer) {
        this.data += cache.data
    }


    companion object {

        inline fun CachedGuiVertexConsumer.addVertex(x: Float, y: Float, uv: PackedUV, texture: ShaderTexture, tint: RGBAColor) = data.add(
            x, y,
            uv.raw,
            texture.shaderId.buffer(),
            tint.rgba.buffer(),
        )


        private fun GUIVertexOptions?.finalTint(tint: RGBAColor): RGBAColor {
            if (this == null) return tint

            var color = tint

            this.tint?.let { color = color.mixRGB(it) }

            if (this.alpha != 1.0f) {
                color = color.with(alpha = color.alphaf * this.alpha)
            }

            return color
        }
    }
}
