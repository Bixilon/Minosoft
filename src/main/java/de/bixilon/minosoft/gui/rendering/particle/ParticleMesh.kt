/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV

class ParticleMesh(context: RenderContext, data: AbstractFloatList) : Mesh(context, ParticleMeshStruct, PrimitiveTypes.POINT, -1, data = data) {

    override fun clear() = Unit

    fun addVertex(position: Vec3d, scale: Float, texture: Texture, tintColor: RGBAColor, uvMin: Vec2f? = null, uvMax: Vec2f? = null, light: Int) {
        val minTransformedUV = if (uvMin == null) texture.transformUVPacked(ZERO) else texture.transformUVPacked(uvMin)
        val maxTransformedUV = if (uvMax == null) texture.transformUVPacked(ONE) else texture.transformUVPacked(uvMax)
        val offset = context.camera.offset.offset

        data.add(
            (position.x - offset.x).toFloat(),
            (position.y - offset.y).toFloat(),
            (position.z - offset.z).toFloat(),

            minTransformedUV,
            maxTransformedUV,

            texture.renderData.shaderTextureId.buffer(),
            scale,
            tintColor.rgba.buffer(),
            light.buffer(),
        )
    }


    data class ParticleMeshStruct(
        val position: Vec3f,
        val minUVCoordinates: PackedUV,
        val maxUVCoordinates: PackedUV,
        val indexLayerAnimation: Int,
        val scale: Float,
        val tintColor: RGBColor,
        val light: Int,
    ) {
        companion object : MeshStruct(ParticleMeshStruct::class)
    }

    private companion object {
        private val ZERO = PackedUV.pack(0.0f, 0.0f)
        private val ONE = PackedUV.pack(1.0f, 1.0f)
    }
}
