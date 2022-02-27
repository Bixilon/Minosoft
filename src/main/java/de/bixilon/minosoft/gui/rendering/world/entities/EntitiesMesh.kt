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

package de.bixilon.minosoft.gui.rendering.world.entities

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class EntitiesMesh(renderWindow: RenderWindow, initialCacheSize: Int) : Mesh(renderWindow, EntitiesMeshStruct, initialCacheSize = initialCacheSize) {

    fun addVertex(position: FloatArray, uv: Vec2, texture: AbstractTexture, tintColor: Int, light: Int) {
        val transformedUV = texture.renderData.transformUV(uv)
        data.add(position[0])
        data.add(position[1])
        data.add(position[2])
        data.add(transformedUV.x)
        data.add(transformedUV.y)
        data.add(Float.fromBits(texture.renderData.shaderTextureId))
        data.add(Float.fromBits(tintColor or (light shl 24)))
    }


    data class EntitiesMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tintLight: Int,
    ) {
        companion object : MeshStruct(EntitiesMeshStruct::class)
    }
}
