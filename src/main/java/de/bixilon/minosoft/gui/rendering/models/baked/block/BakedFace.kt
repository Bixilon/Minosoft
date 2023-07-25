/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.getMesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rgb

class BakedFace(
    override val sizeStart: Vec2,
    override val sizeEnd: Vec2,
    val positions: FloatArray,
    val uv: Array<Vec2>,
    val shade: Float,
    val tintIndex: Int,
    val cullFace: Directions?,
    val texture: Texture,
    val touching: Boolean,
) : AbstractFaceProperties {
    override val transparency: TextureTransparencies
        get() = texture.transparency // ToDo

    fun singleRender(position: FloatArray, mesh: WorldMesh, light: Int, tint: Int) {
        val meshToUse = transparency.getMesh(mesh)
        // ToDo: Ambient light
        val color = Vec3(shade)
        if (tint >= 0) {
            color.r *= (tint shr 16) / RGBColor.COLOR_FLOAT_DIVIDER
            color.g *= ((tint shr 8) and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
            color.b *= (tint and 0xFF) / RGBColor.COLOR_FLOAT_DIVIDER
        }

        val textureShaderId = texture.renderData.shaderTextureId.buffer()
        val tintLight = (color.rgb or (light shl 24)).buffer()
        for ((index, textureIndex) in meshToUse.order) {
            val indexOffset = index * 3
            meshToUse.addVertex(positions[indexOffset + 0] + position[0], positions[indexOffset + 1] + position[1], positions[indexOffset + 2] + position[2], uv[textureIndex], texture, textureShaderId, tintLight)
        }
    }
}
