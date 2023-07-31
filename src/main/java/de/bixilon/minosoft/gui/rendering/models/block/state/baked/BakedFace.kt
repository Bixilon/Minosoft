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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.SingleChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.preparer.SolidSectionMesher.Companion.SELF_LIGHT_INDEX
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rgb

class BakedFace(
    val positions: FloatArray,
    val uv: FloatArray,
    val shade: Float,
    val tintIndex: Int,
    cull: Directions?,
    val texture: Texture,
    val properties: FaceProperties? = null,
) {
    private val lightIndex = cull?.ordinal ?: SELF_LIGHT_INDEX

    private fun color(tint: Int): Int {
        val color = Vec3(this.shade)
        if (tint > 0) {
            color.r *= (tint shr 16) / RGBColor.COLOR_FLOAT_DIVIDER
            color.g *= ((tint shr 8) and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
            color.b *= (tint and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
        }
        return color.rgb
    }

    fun render(offset: FloatArray, mesh: ChunkMesh, light: ByteArray, tints: IntArray?) {
        val tint = color(tints?.getOrNull(tintIndex) ?: 0)
        val lightTint = ((light[lightIndex].toInt() shl 24) or tint).buffer()
        val textureId = this.texture.shaderId.buffer()


        val mesh = mesh.mesh(texture)

        for (index in 0 until mesh.order.size step 2) {
            val vertexOffset = mesh.order[index] * 3
            val uvOffset = mesh.order[index + 1] * 2

            mesh.addVertex(
                x = offset[0] + positions[vertexOffset], y = offset[1] + positions[vertexOffset + 1], z = offset[2] + positions[vertexOffset + 2],
                uv = floatArrayOf(uv[uvOffset], uv[uvOffset + 1]),
                texture = this.texture,
                shaderTextureId = textureId,
                lightTint = lightTint,
            )
        }
    }

    private fun ChunkMesh.mesh(texture: Texture): SingleChunkMesh {
        return when (texture.transparency) {
            TextureTransparencies.OPAQUE -> opaqueMesh
            TextureTransparencies.TRANSPARENT -> transparentMesh
            TextureTransparencies.TRANSLUCENT -> translucentMesh
        }!!
    }
}
