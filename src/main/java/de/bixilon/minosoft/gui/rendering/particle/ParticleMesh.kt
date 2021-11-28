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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class ParticleMesh(renderWindow: RenderWindow, data: DirectArrayFloatList) : Mesh(renderWindow, ParticleMeshStruct, PrimitiveTypes.POINT, -1, clearOnLoad = false, data = data) {

    fun addVertex(position: Vec3d, scale: Float, texture: AbstractTexture, tintColor: RGBColor, uvMin: Vec2 = Vec2(0.0f, 0.0f), uvMax: Vec2 = Vec2(1.0f, 1.0f)) {
        val minTransformedUV = texture.renderData?.transformUV(uvMin) ?: uvMin
        val maxTransformedUV = texture.renderData?.transformUV(uvMax) ?: uvMax
        data.addAll(
            floatArrayOf(
                position.x.toFloat(), // ToDo: Use doubles
                position.y.toFloat(),
                position.z.toFloat(),
                minTransformedUV.x,
                minTransformedUV.y,
                maxTransformedUV.x,
                maxTransformedUV.y,
                Float.fromBits(texture.renderData?.shaderTextureId ?: RenderConstants.DEBUG_TEXTURE_ID),
                scale,
                Float.fromBits(tintColor.rgba),
            ))
    }


    data class ParticleMeshStruct(
        val position: Vec3,
        val minUVCoordinates: Vec2,
        val maxUVCoordinates: Vec2,
        val indexLayerAnimation: Int,
        val scale: Float,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(ParticleMeshStruct::class)
    }
}
