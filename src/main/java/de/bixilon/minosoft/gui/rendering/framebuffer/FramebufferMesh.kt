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

package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV


class FramebufferMesh(context: RenderContext) : Mesh(context, DefaultFramebufferMeshStruct) {

    init {
        val vertices = arrayOf(
            Vec2(-1.0f, -1.0f) to Vec2(0.0f, 1.0f),
            Vec2(-1.0f, +1.0f) to Vec2(0.0f, 0.0f),
            Vec2(+1.0f, +1.0f) to Vec2(1.0f, 0.0f),
            Vec2(+1.0f, -1.0f) to Vec2(1.0f, 1.0f),
        )
        order.iterate { position, uv -> addVertex(vertices[position].first, vertices[uv].second) }
    }

    private fun addVertex(position: Vec2, uv: Vec2) {
        data.add(position.array)
        data.add(uv.array)
    }

    data class DefaultFramebufferMeshStruct(
        val position: Vec2,
        val uv: UnpackedUV,
    ) {
        companion object : MeshStruct(DefaultFramebufferMeshStruct::class)
    }
}
