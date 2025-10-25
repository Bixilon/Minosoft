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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.mat.mat4.f._Mat4f
import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshBuilder
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture


class WorldGUIConsumer(val mesh: ChunkMeshBuilder, val transform: Mat4f, val light: Int) : GUIVertexConsumer {
    private val whiteTexture = mesh.context.textures.whiteTexture
    override val order: RenderOrder get() = mesh.order
    private val uv = MVec2f() // temporary

    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        val transformed = transform.fastTimes(x / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION, -y / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION)
        this.uv.x = u; this.uv.y = v
        mesh.addVertex(transformed, this.uv.unsafe, texture ?: whiteTexture.texture, tint.rgb(), light)
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
    override fun addCache(cache: GUIMeshCache) {
        throw IllegalStateException("This is not hud!")
    }

    override fun ensureSize(size: Int) {
        mesh.data.ensureSize(size)
    }

    private inline fun _Mat4f.fastTimes(x: Float, y: Float) = Vec3f(
        this[0, 0] * x + this[1, 0] * y + this[2, 0] + this[3, 0],
        this[0, 1] * x + this[1, 1] * y + this[2, 1] + this[3, 1],
        this[0, 2] * x + this[1, 2] * y + this[2, 2] + this[3, 2],
    )
}
