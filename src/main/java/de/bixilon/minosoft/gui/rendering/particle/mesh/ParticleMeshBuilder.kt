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

package de.bixilon.minosoft.gui.rendering.particle.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV

class ParticleMeshBuilder(context: RenderContext, data: FloatList) : MeshBuilder(context, ParticleMeshStruct, PrimitiveTypes.POINT, -1, data = data) {
    override val reused get() = true

    inline fun addVertex(x: Float, y: Float, z: Float, minUV: PackedUV, maxUV: PackedUV, texture: Texture, scale: Float, tint: RGBAColor, light: Int) = data.add(
        x, y, z,
        minUV.raw, maxUV.raw,
        texture.shaderId.buffer(),
        scale,
        tint.rgba.buffer(),
        light.buffer(),
    )


    fun addVertex(position: Vec3d, scale: Float, texture: Texture, tint: RGBAColor, uvMin: Vec2f? = null, uvMax: Vec2f? = null, light: Int) {
        val minTransformedUV = if (uvMin == null) texture.transformUV(PackedUV.ZERO) else texture.transformUV(PackedUV(uvMin))
        val maxTransformedUV = if (uvMax == null) texture.transformUV(PackedUV.ONE) else texture.transformUV(PackedUV(uvMax))
        val offset = context.camera.offset.offset

        addVertex(
            (position.x - offset.x).toFloat(), (position.y - offset.y).toFloat(), (position.z - offset.z).toFloat(),
            minTransformedUV, maxTransformedUV,
            texture,
            scale,
            tint,
            light,
        )
    }


    data class ParticleMeshStruct(
        val position: Vec3f,
        val minUV: PackedUV,
        val maxUV: PackedUV,
        val texture: Int,
        val scale: Float,
        val tint: RGBColor,
        val light: Int,
    ) {
        companion object : MeshStruct(ParticleMeshStruct::class)
    }
}
