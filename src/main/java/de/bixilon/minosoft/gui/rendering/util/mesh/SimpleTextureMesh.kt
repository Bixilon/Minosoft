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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class SimpleTextureMesh(renderWindow: RenderWindow, primitiveType: PrimitiveTypes = renderWindow.renderSystem.preferredPrimitiveType) : Mesh(renderWindow, SimpleTextureMeshStruct, primitiveType, initialCacheSize = 2 * 3 * SimpleTextureMeshStruct.FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, texture: AbstractTexture, uv: Vec2, tintColor: RGBColor) {
        data.addAll(
            floatArrayOf(
                position.x,
                position.y,
                position.z,
                uv.x,
                uv.y,
                Float.fromBits(texture.renderData.shaderTextureId),
                Float.fromBits(tintColor.rgba)
            ))
    }


    data class SimpleTextureMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tint: RGBColor,
    ) {
        companion object : MeshStruct(SimpleTextureMeshStruct::class)
    }
}
