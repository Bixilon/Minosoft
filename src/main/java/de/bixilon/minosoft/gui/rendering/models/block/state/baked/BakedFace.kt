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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.chunk.mesher.SolidSectionMesher.Companion.SELF_LIGHT_INDEX
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades.Companion.shade
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.tint.TintUtil

class BakedFace(
    val positions: FaceVertexData,
    val uv: FaceVertexData,
    val shade: Shades,
    val tintIndex: Int,
    cull: Directions?,
    val texture: Texture,
    val properties: FaceProperties? = null,
) {
    private val lightIndex = cull?.ordinal ?: SELF_LIGHT_INDEX


    constructor(positions: FaceVertexData, uv: FaceVertexData, shade: Boolean, tintIndex: Int, texture: Texture, direction: Directions, properties: FaceProperties?) : this(positions, uv, if (shade) direction.shade else Shades.NONE, tintIndex, if (properties == null) null else direction, texture, properties)

    private fun color(tint: Int): Int {
        if (tint <= 0) return shade.color
        return TintUtil.calculateTint(tint, shade)
    }

    fun render(offset: FloatArray, mesh: BlockVertexConsumer, light: ByteArray, tints: IntArray?) {
        val tint = color(tints?.getOrNull(tintIndex) ?: 0)
        val lightTint = ((light[lightIndex].toInt() shl 24) or tint).buffer()
        val textureId = this.texture.shaderId.buffer()


        val mesh = mesh[texture.transparency]
        mesh.addQuad(offset, this.positions, this.uv, textureId, lightTint)
    }


    fun IntArray.getOrNull(index: Int): Int? {
        return if (index >= 0 && index < size) get(index) else null
    }
}
