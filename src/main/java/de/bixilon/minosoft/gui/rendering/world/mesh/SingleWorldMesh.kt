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

package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SingleWorldMesh(renderWindow: RenderWindow, initialCacheSize: Int) : Mesh(renderWindow, SectionArrayMeshStruct, initialCacheSize = initialCacheSize) {
    var distance: Float = 0.0f // Used for sorting

    fun addVertex(position: FloatArray, uv: Vec2, texture: AbstractTexture, tintColor: Int, light: Int) {
        val transformedUV = texture.renderData?.transformUV(uv) ?: uv
        data.add(position[0])
        data.add(position[1])
        data.add(position[2])
        data.add(transformedUV.x)
        data.add(transformedUV.y)
        data.add(Float.fromBits(texture.renderData?.shaderTextureId ?: RenderConstants.DEBUG_TEXTURE_ID))
        data.add(Float.fromBits(tintColor or (light shl 24)))
    }


    data class SectionArrayMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tintLight: Int,
    ) {
        companion object : MeshStruct(SectionArrayMeshStruct::class)
    }
}
