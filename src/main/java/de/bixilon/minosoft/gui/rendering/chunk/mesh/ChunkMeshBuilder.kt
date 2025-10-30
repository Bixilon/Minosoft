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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadConsumer.Companion.iterate
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray

class ChunkMeshBuilder(context: RenderContext, estimate: Int) : QuadMeshBuilder(context, ChunkMeshStruct, estimate), BlockVertexConsumer {

    inline fun addVertex(x: Float, y: Float, z: Float, aUV: Float, texture: ShaderTexture, lightTint: Int) = data.add(
        x, y, z,
        aUV,
        texture.shaderId.buffer(),
        lightTint.buffer(),
    )

    fun addVertex(x: Float, y: Float, z: Float, ao: Int, uv: PackedUV, texture: ShaderTexture, light: Int, tint: RGBColor) {
        val aUV = Float.fromBits(uv.raw.toBits() or (ao shl 24))
        val lightTint = (light and 0xFF shl 24) or tint.rgb

        addVertex(x, y, z, aUV, texture, lightTint)
    }


    override fun addQuad(offset: Vec3f, positions: FaceVertexData, uv: PackedUVArray, texture: ShaderTexture, light: Int, tint: RGBColor, ao: IntArray) {
        ensureSize(1)

        val lightTint = (light and 0xFF shl 24) or tint.rgb


        iterate {
            val vertexOffset = it * Vec3f.LENGTH


            val aUV = Float.fromBits(uv.raw[it].toBits() or (ao[it] shl 24))
            addVertex(
                offset.x + positions[vertexOffset], offset.y + positions[vertexOffset + 1], offset.z + positions[vertexOffset + 2],
                aUV,
                texture,
                lightTint
            )
        }
        addIndexQuad()
    }

    override fun bake() = ChunkMesh(createVertexBuffer())

    data class ChunkMeshStruct(
        val position: Vec3f,
        val uv: PackedUV,
        val texture: ShaderTexture,
        val lightTint: Int,
    ) {
        companion object : MeshStruct(ChunkMeshStruct::class)
    }
}
