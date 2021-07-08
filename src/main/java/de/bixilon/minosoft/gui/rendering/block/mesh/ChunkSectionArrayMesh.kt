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

package de.bixilon.minosoft.gui.rendering.block.mesh

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class ChunkSectionArrayMesh(renderWindow: RenderWindow) : Mesh(renderWindow, SectionArrayMeshStruct, initialCacheSize = 100000) {

    fun addVertex(position: Vec3, textureCoordinates: Vec2, texture: Texture, tintColor: RGBColor?, light: Int) {
        val color = tintColor ?: ChatColors.WHITE

        val textureLayer = if (RenderConstants.FORCE_DEBUG_TEXTURE) {
            RenderConstants.DEBUG_TEXTURE_ID
        } else {
            (texture.arrayId shl 24) or texture.arrayLayer
        }
        data.addAll(
            floatArrayOf(
            position.x,
            position.y,
            position.z,
            textureCoordinates.x * texture.uvEnd.x,
            textureCoordinates.y * texture.uvEnd.y,
            Float.fromBits(textureLayer),
            Float.fromBits(texture.properties.animation?.animationId ?: -1),
            Float.fromBits(color.rgb),
            Float.fromBits(light),
        ))
    }


    data class SectionArrayMeshStruct(
        val position: Vec3,
        val uvCoordinates: Vec2,
        val textureLayer: Int,
        val animationId: Int,
        val tintColor: RGBColor,
        val light: Int,
    ) {
        companion object : MeshStruct(SectionArrayMeshStruct::class)
    }
}
