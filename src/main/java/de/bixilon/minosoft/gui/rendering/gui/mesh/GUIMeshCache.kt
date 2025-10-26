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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.collections.primitive.floats.HeapFloatList
import de.bixilon.kutil.collections.primitive.ints.HeapIntList
import de.bixilon.kutil.collections.primitive.ints.IntList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.IndexUtil

class GUIMeshCache(
    var halfSize: Vec2f,
    val context: RenderContext,
    estimate: Int = 12,
    var data: FloatList = HeapFloatList(estimate * PrimitiveTypes.QUAD.vertices * GUIMeshBuilder.GUIMeshStruct.floats),
    var index: IntList = HeapIntList(estimate),
) : GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture

    var revision = 0L
    var offset: Vec2f = Vec2f.EMPTY
    var options: GUIVertexOptions? = null

    fun clear() {
        data.clear()
        index.clear()
    }


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        GUIMeshBuilder.addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
        revision++
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        GUIMeshBuilder.addVertex(data, halfSize, x, y, textureId, u, v, tint, options)
        revision++
    }

    override fun addCache(cache: GUIMeshCache) {
        data += cache.data
        index += cache.index
        revision++
    }

    override fun ensureSize(vertices: Int) {
        data.ensureSize(vertices)
    }

    override fun addIndexQuad(front: Boolean, reverse: Boolean) {
        IndexUtil.addTriangleQuad(index, front, reverse)
    }
}
