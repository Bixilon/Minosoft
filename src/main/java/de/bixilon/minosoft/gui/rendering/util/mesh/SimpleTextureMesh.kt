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
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class SimpleTextureMesh(renderWindow: RenderWindow) : Mesh(renderWindow, SimpleTextureMeshStruct, initialCacheSize = 2 * 3 * SimpleTextureMeshStruct.FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, texture: AbstractTexture, textureCoordinates: Vec2, tintColor: RGBColor) {
        val textureLayer = if (RenderConstants.FORCE_DEBUG_TEXTURE) {
            RenderConstants.DEBUG_TEXTURE_ID
        } else {
            texture.renderData?.layer ?: RenderConstants.DEBUG_TEXTURE_ID
        }

        data.addAll(
            floatArrayOf(
                position.x,
                position.y,
                position.z,
                textureCoordinates.x,
                textureCoordinates.y,
                Float.fromBits(textureLayer),
                Float.fromBits(tintColor.rgba),
            ))
    }


    data class SimpleTextureMeshStruct(
        val position: Vec3,
        val uvCoordinates: Vec2,
        val textureLayer: Int,
        val animationId: Int,
    ) {
        companion object : MeshStruct(SimpleTextureMeshStruct::class)
    }
}
