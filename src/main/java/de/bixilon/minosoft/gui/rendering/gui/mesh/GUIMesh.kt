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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2t
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class GUIMesh(
    renderWindow: RenderWindow,
    val matrix: Mat4,
) : Mesh(renderWindow, HUDMeshStruct), GUIVertexConsumer {

    override fun addVertex(position: Vec2t<*>, texture: AbstractTexture, uv: Vec2, tint: RGBColor) {
        val outPosition = matrix * Vec4(position.x.toFloat(), position.y.toFloat(), 1.0f, 1.0f)
        data.addAll(floatArrayOf(
            outPosition.x,
            outPosition.y,
            0.95f,
            uv.x,
            uv.y,
            Float.fromBits(texture.renderData?.layer ?: RenderConstants.DEBUG_TEXTURE_ID),
            Float.fromBits(tint.rgba),
        ))
    }

    data class HUDMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val textureLayer: Int,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(HUDMeshStruct::class)
    }
}
