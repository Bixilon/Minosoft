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
import de.bixilon.kutil.collections.primitive.ints.IntList
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.CachedGuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.IndexUtil
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV

class GuiMeshBuilder(
    context: RenderContext,
    override val halfSize: Vec2f,
    data: FloatList,
    index: IntList,
) : QuadMeshBuilder(context, GUIMeshStruct, 0, data = data, index = index), CachedGuiVertexConsumer {
    override val white = context.textures.whiteTexture.texture

    override val reused get() = true

    override fun addIndexQuad(front: Boolean, reverse: Boolean) = Broken("Who dares?")

    fun fixIndex() {
        val vertices = data.size / struct.floats

        val multiplier = (if (remap) (2 * PrimitiveTypes.TRIANGLE.vertices) else PrimitiveTypes.QUAD.vertices)
        this.index.ensureSize(vertices * multiplier)

        var start = index.size / multiplier
        val native = index.toUnsafeNativeBuffer()
        if (native == null) {
            if (start > vertices) {
                index.clear() // TODO: Optimize (just fake size down)
                start = 0
            }
        } else {
            native.limit(vertices * multiplier)
        }

        for (offset in start until vertices) {
            if (remap) {
                IndexUtil.addTriangleQuad(index, offset * PrimitiveTypes.QUAD.vertices, true, false)
            } else {
                // That could be left out (=> no index buffer)
                IndexUtil.addNativeQuad(index, offset * PrimitiveTypes.QUAD.vertices, true, false)
            }
        }
    }

    data class GUIMeshStruct(
        val position: Vec2f,
        val uv: PackedUV,
        val texture: ShaderTexture,
        val tint: RGBAColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }
}
