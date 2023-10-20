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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.mesh.SingleChunkMesh
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture


class WorldGUIConsumer(val mesh: SingleChunkMesh, val transform: Mat4, val light: Int) : GUIVertexConsumer {
    private val whiteTexture = mesh.context.textures.whiteTexture
    override val order: RenderOrder get() = mesh.order

    override fun addVertex(position: Vec2, texture: ShaderTexture?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        val transformed = transform.fastTimes(position.x / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION, -position.y / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION)
        mesh.addVertex(transformed, uv, (texture as Texture?) ?: whiteTexture.texture, tint.rgb, light)
    }

    override fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
        // TODO: remove that override and change vertex order globally
        val topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * FormattingProperties.ITALIC_OFFSET else 0.0f

        val positions = arrayOf(
            Vec2(start.x + topOffset, start.y),
            Vec2(end.x + topOffset, start.y),
            end,
            Vec2(start.x, end.y),
        )
        val texturePositions = arrayOf(
            uvStart,
            Vec2(uvEnd.x, uvStart.y),
            uvEnd,
            Vec2(uvStart.x, uvEnd.y),
        )

        order.iterate { position, uv -> addVertex(positions[position], texture, texturePositions[uv], tint, options) }
    }

    override fun addCache(cache: GUIMeshCache) {
        throw IllegalStateException("This is not hud!")
    }

    override fun ensureSize(size: Int) {
        mesh.data.ensureSize(size)
    }

    private fun Mat4.fastTimes(x: Float, y: Float): FloatArray {
        return floatArrayOf(
            this[0, 0] * x + this[1, 0] * y + this[2, 0] + this[3, 0],
            this[0, 1] * x + this[1, 1] * y + this[2, 1] + this[3, 1],
            this[0, 2] * x + this[1, 2] * y + this[2, 2] + this[3, 2],
        )
    }
}
