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
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rgb
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.SolidCullSectionPreparer.Companion.SELF_LIGHT_INDEX

class BakedFace(
    val positions: FloatArray,
    val uv: FloatArray,
    val shade: Float,
    val tintIndex: Int,
    val cull: Directions?,
    val texture: AbstractTexture,
) {
    private var cullIndex = cull?.ordinal ?: SELF_LIGHT_INDEX

    private fun color(tint: Int): Int {
        val color = Vec3(this.shade)
        if (tint > 0) {
            color.r *= (tint shr 16) / RGBColor.COLOR_FLOAT_DIVIDER
            color.g *= ((tint shr 8) and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
            color.b *= (tint and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
        }
        return color.rgb
    }

    fun render(offset: FloatArray, mesh: WorldMesh, light: ByteArray, tints: IntArray?) {
        val tint = color(tints?.getOrNull(tintIndex) ?: 0)
        val tintLight = ((light[cullIndex].toInt() shl 24) or tint).buffer()
        val textureId = this.texture.shaderId.buffer()


        val mesh = mesh.opaqueMesh!! // TODO: use correct mesh

        for ((vertexIndex, textureIndex) in mesh.order) {
            val vertexOffset = vertexIndex * 3
            val uvOffset = textureIndex * 2

            mesh.addVertex(
                x = offset[0] + positions[vertexOffset], y = offset[1] + positions[vertexOffset + 1], z = offset[2] + positions[vertexOffset + 2],
                uv = floatArrayOf(uv[uvOffset], uv[uvOffset + 1]),
                texture = this.texture,
                shaderTextureId = textureId,
                tintLight = tintLight,
            )
        }
    }
}
