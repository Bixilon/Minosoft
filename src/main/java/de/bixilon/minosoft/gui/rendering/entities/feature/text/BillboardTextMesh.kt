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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class BillboardTextMesh(context: RenderContext) : Mesh(context, BillboardTextMeshStruct), GUIVertexConsumer {

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }

    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?) {
        data.add(x * SCALE, y * SCALE)
        data.add(u, v)
        data.add(texture?.shaderId?.buffer() ?: 0.0f)
        data.add(tint.rgb.buffer())
    }

    override fun addCache(cache: GUIMeshCache) = Broken("This is not a text only consumer!")

    data class BillboardTextMeshStruct(
        val position: Vec2,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val lightTint: Int,
    ) {
        companion object : MeshStruct(BillboardTextMeshStruct::class)
    }

    private companion object {
        const val SCALE = 0.03f
    }
}
