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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.weather

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

open class WeatherOverlayMesh(context: RenderContext, primitiveType: PrimitiveTypes = context.renderSystem.preferredPrimitiveType) : Mesh(context, WeatherOverlayMeshStruct, primitiveType, initialCacheSize = 2 * 3 * WeatherOverlayMeshStruct.FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, uv: Vec2, offset: Float, offsetMultiplicator: Float, alphaMultiplicator: Float) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(uv.x)
        data.add(uv.y)
        data.add(offset)
        data.add(offsetMultiplicator)
        data.add(alphaMultiplicator)
    }


    data class WeatherOverlayMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val offset: Float,
        val vinOffsetMultiplicator: Float,
        val alphaMultiplicator: Float,
    ) {
        companion object : MeshStruct(WeatherOverlayMeshStruct::class)
    }
}
