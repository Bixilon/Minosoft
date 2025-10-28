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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.CharVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class BillboardTextMeshBuilder(context: RenderContext) : QuadMeshBuilder(context, BillboardTextMeshStruct), CharVertexConsumer {

    inline fun addVertex(x: Float, y: Float, u: Float, v: Float, texture: ShaderTexture, tint: RGBAColor) = data.add(
        x * SCALE, y * SCALE,
        u, v,
        texture.shaderId.buffer(),
        tint.rgba.buffer(),
    )

    override fun addChar(start: Vec2f, end: Vec2f, texture: ShaderTexture, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
        val topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * FormattingProperties.ITALIC_OFFSET else 0.0f

        // uv is already pretransformed

        addVertex(start.x + topOffset, start.y, uvStart.x, uvStart.y, texture, tint)
        addVertex(start.x, end.y, uvStart.x, uvEnd.y, texture, tint)
        addVertex(end.x, end.y, uvEnd.x, uvEnd.y, texture, tint)
        addVertex(end.x + topOffset, start.y, uvEnd.x, uvStart.y, texture, tint)

        addIndexQuad()
    }

    inline fun addVertex(x: Float, y: Float, tint: RGBAColor) = data.add(
        x * SCALE, y * SCALE,
        0.0f, 0.0f,
        context.textures.whiteTexture.texture.shaderId.buffer(),
        tint.rgba.buffer(),
    )

    override fun addQuad(start: Vec2f, end: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) {
        addVertex(start.x, start.y, tint)
        addVertex(start.x, end.y, tint)
        addVertex(end.x, end.y, tint)
        addVertex(end.x, start.y, tint)

        addIndexQuad()
    }


    data class BillboardTextMeshStruct(
        val position: Vec2f,
        val uv: Vec2f,
        val texture: ShaderTexture,
        val tint: RGBAColor,
    ) {
        companion object : MeshStruct(BillboardTextMeshStruct::class)
    }

    companion object {
        const val SCALE = 0.02f
    }
}
