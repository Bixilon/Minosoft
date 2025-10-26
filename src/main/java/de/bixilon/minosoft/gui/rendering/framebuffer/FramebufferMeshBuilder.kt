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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV


class FramebufferMeshBuilder(context: RenderContext) : MeshBuilder(context, DefaultFramebufferMeshStruct) {
    override val order = context.system.legacyQuadOrder

    init {
        order.iterate { position, uv -> addVertex(VERTICES[position].first, VERTICES[uv].second) }
        addIndexQuad()
    }

    private fun addVertex(position: Vec2f, uv: Vec2f) {
        data.add(position.x, position.y)
        data.add(uv.x, uv.y)
    }

    data class DefaultFramebufferMeshStruct(
        val position: Vec2f,
        val uv: UnpackedUV,
    ) {
        companion object : MeshStruct(DefaultFramebufferMeshStruct::class)
    }

    companion object {
        val VERTICES = arrayOf(
            Vec2f(-1.0f, -1.0f) to Vec2f(0.0f, 1.0f),
            Vec2f(-1.0f, +1.0f) to Vec2f(0.0f, 0.0f),
            Vec2f(+1.0f, +1.0f) to Vec2f(1.0f, 0.0f),
            Vec2f(+1.0f, -1.0f) to Vec2f(1.0f, 1.0f),
        )
    }
}
