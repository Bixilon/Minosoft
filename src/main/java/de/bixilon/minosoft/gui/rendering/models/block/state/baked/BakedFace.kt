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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.chunk.mesher.SolidSectionMesher.Companion.SELF_LIGHT_INDEX
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades.Companion.shade
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.tint.TintUtil
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class BakedFace(
    val positions: FaceVertexData,
    val uv: UnpackedUV,
    val shade: Shades,
    val tintIndex: Int,
    cull: Directions?,
    val texture: Texture,
    val properties: FaceProperties? = null,
) {
    val packedUV = uv.pack()
    private val lightIndex = cull?.ordinal ?: SELF_LIGHT_INDEX


    constructor(positions: FaceVertexData, uv: UnpackedUV, shade: Boolean, tintIndex: Int, texture: Texture, direction: Directions, properties: FaceProperties?) : this(positions, uv, if (shade) direction.shade else Shades.NONE, tintIndex, if (properties == null) null else direction, texture, properties)

    private fun color(tint: RGBColor): RGBColor {
        if (tint.rgb <= 0) return shade.color
        return TintUtil.calculateTint(tint, shade)
    }

    fun render(offset: Vec3f, mesh: BlockVertexConsumer, light: ByteArray, tints: RGBArray?, ao: IntArray) {
        val tint = color(tints.getOr0(tintIndex))
        val lightTint = ((light[lightIndex].toInt() and 0xFF shl 24) or tint.rgb).buffer()
        val textureId = this.texture.shaderId.buffer()


        val mesh = mesh[texture.transparency]
        mesh.addQuad(offset, this.positions, packedUV, textureId, lightTint, ao)
    }

    fun render(mesh: BlockVertexConsumer, tints: RGBArray?) {
        val tint = color(tints.getOr0(tintIndex))
        val lightTint = tint.rgb.buffer()
        val textureId = this.texture.shaderId.buffer()

        val mesh = mesh[texture.transparency]
        mesh.addQuad(this.positions, this.uv, textureId, lightTint)
    }


    fun RGBArray?.getOr0(index: Int): RGBColor {
        if (this == null) return RGBColor(0)
        return if (index >= 0 && index < size) get(index) else RGBColor(0)
    }
}
