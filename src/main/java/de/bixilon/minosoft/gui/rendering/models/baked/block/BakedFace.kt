/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.baked.block

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.models.FaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rgb
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class BakedFace(
    override val sizeStart: Vec2,
    override val sizeEnd: Vec2,
    val positions: Array<Vec3>,
    val uv: Array<Vec2>,
    val shade: Float,
    val tintIndex: Int,
    val cullFace: Directions?,
    val texture: AbstractTexture,
    val touching: Boolean,
) : FaceProperties {
    override val transparency: TextureTransparencies
        get() = texture.transparency // ToDo

    fun singleRender(position: FloatArray, mesh: ChunkSectionMeshes, light: Int, ambientLight: FloatArray, tint: Int) {
        val meshToUse = when (texture.transparency) {
            TextureTransparencies.OPAQUE -> mesh.opaqueMesh
            TextureTransparencies.TRANSLUCENT -> mesh.translucentMesh
            TextureTransparencies.TRANSPARENT -> mesh.transparentMesh
        }!!
        // ToDo: Ambient light
        val color = Vec3(shade)
        if (tint >= 0) {
            color.r *= (tint shr 16) / RGBColor.COLOR_FLOAT_DIVIDER
            color.g *= ((tint shr 8) and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
            color.b *= (tint and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
        }
        for ((index, textureIndex) in meshToUse.order) {
            val indexPosition = positions[index].array
            meshToUse.addVertex(floatArrayOf(indexPosition[0] + position[0], indexPosition[1] + position[1], indexPosition[2] + position[2]), uv[textureIndex], texture, color.rgb, light)
        }
    }

    fun greedyRender(start: Vec3, end: Vec3, side: Directions, mesh: ChunkSectionMesh, light: Int) {
        val multiplier = end - start
        val positions = arrayOf(
            (positions[0] * multiplier) + start,
            (positions[1] * multiplier) + start,
            (positions[2] * multiplier) + start,
            (positions[3] * multiplier) + start,
        )
        val fixPosition = this.positions[0][side.axis]
        for (position in positions) {
            when (side.axis) {
                Axes.X -> position.x = start.x + fixPosition
                Axes.Y -> position.y = start.y + fixPosition
                Axes.Z -> position.z = start.z + fixPosition
            }
        }

        val uvMultiplier = side.getUVMultiplier(start, end)
        val uv = arrayOf(
            uv[0] * uvMultiplier,
            uv[1] * uvMultiplier,
            uv[2] * uvMultiplier,
            uv[3] * uvMultiplier,
        )
        for ((index, textureIndex) in mesh.order) {
            // ToDo
            mesh.addVertex(positions[index].array, uv[textureIndex], texture, 0xFFFFFF, light)
        }
    }
}
