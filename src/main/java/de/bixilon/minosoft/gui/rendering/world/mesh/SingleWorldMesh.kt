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

package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class SingleWorldMesh(context: RenderContext, initialCacheSize: Int, onDemand: Boolean = false) : Mesh(context, WorldMeshStruct, initialCacheSize = initialCacheSize, onDemand = onDemand) {
    var distance: Float = 0.0f // Used for sorting

    override val order = if (quadType == PrimitiveTypes.QUAD) QUAD_ORDER else TRIANGLE_ORDER

    fun addVertex(position: FloatArray, uv: Vec2, texture: AbstractTexture, tintColor: Int, light: Int) {
        data.ensureSize(WorldMeshStruct.FLOATS_PER_VERTEX)
        val transformedUV = texture.renderData.transformUV(uv).array
        data.add(position[0])
        data.add(position[1])
        data.add(position[2])
        data.add(transformedUV)
        data.add(texture.renderData.shaderTextureId.buffer())
        data.add(((light shl 24) or tintColor).buffer())
    }

    fun addVertex(x: Float, y: Float, z: Float, uv: FloatArray, texture: AbstractTexture, shaderTextureId: Float, lightTint: Float) {
        data.ensureSize(WorldMeshStruct.FLOATS_PER_VERTEX)
        val transformedUV = texture.renderData.transformUV(uv)
        data.add(x)
        data.add(y)
        data.add(z)
        data.add(transformedUV)
        data.add(shaderTextureId)
        data.add(lightTint)
    }


    data class WorldMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val lightTint: Int,
    ) {
        companion object : MeshStruct(WorldMeshStruct::class)
    }

    companion object {
        // TODO: uv coordinates should start in the upper left corner, then a 0=>0 mapping is possible
        val TRIANGLE_ORDER = intArrayOf(
            0, 0,
            3, 3,
            2, 2,
            2, 2,
            1, 1,
            0, 0,
        )
        val QUAD_ORDER = intArrayOf(
            0, 0,
            3, 3,
            2, 2,
            1, 1,
        )
    }
}
