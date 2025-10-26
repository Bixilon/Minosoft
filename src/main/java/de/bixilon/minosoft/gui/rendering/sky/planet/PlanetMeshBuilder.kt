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

package de.bixilon.minosoft.gui.rendering.sky.planet

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

open class PlanetMeshBuilder(context: RenderContext, primitiveType: PrimitiveTypes = context.system.quadType) : MeshBuilder(context, SunMeshStruct, primitiveType, initialCacheSize = 2 * 3 * SunMeshStruct.floats) {

    fun addVertex(position: Vec3f, texture: Texture, uv: Vec2f) {
        data.add(
            position.x, position.y, position.z,
            uv.x, uv.y,
            texture.renderData.shaderTextureId.buffer(),
        )
    }


    data class SunMeshStruct(
        val position: Vec3f,
        val uv: UnpackedUV,
        val indexLayerAnimation: Int,
    ) {
        companion object : MeshStruct(SunMeshStruct::class)
    }
}
