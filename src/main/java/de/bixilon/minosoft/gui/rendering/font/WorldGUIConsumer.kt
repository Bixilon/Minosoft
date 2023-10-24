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
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture


class WorldGUIConsumer(val mesh: SingleChunkMesh, val transform: Mat4, val light: Int) : GUIVertexConsumer {
    private val whiteTexture = mesh.context.textures.whiteTexture
    override val order: RenderOrder get() = mesh.order
    private val uv = Vec2()

    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?) {
        val transformed = transform.fastTimes(x / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION, -y / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION)
        this.uv.x = u; this.uv.y = v
        mesh.addVertex(transformed, this.uv, texture ?: whiteTexture.texture, tint.rgb, light)
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
