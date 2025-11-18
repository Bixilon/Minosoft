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

package de.bixilon.minosoft.gui.rendering.chunk.border.world.mesh

import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class WorldBorderMeshBuilder(
    context: RenderContext,
    val offset: BlockPosition,
    val center: Vec2d,
    val radius: Double,
) : QuadMeshBuilder(context, WorldBorderMeshStruct, 4) {

    init {
        val radius = minOf(radius.toFloat(), World.MAX_VIEW_DISTANCE.toFloat() * ChunkSize.SECTION_WIDTH_X)

        val x1 = (maxOf(-WorldBorder.MAX_RADIUS, center.x - radius) - offset.x).toFloat()
        val y1 = (maxOf(-WorldBorder.MAX_RADIUS, center.y - radius) - offset.z).toFloat()

        val x2 = (minOf(WorldBorder.MAX_RADIUS, center.x + radius) - offset.x).toFloat()
        val y2 = (minOf(WorldBorder.MAX_RADIUS, center.y + radius) - offset.z).toFloat()

        // north
        addVertex(x1, -1.0f, y1, 2, radius)
        addVertex(x1, +1.0f, y1, 3, radius)
        addVertex(x2, +1.0f, y1, 0, radius)
        addVertex(x2, -1.0f, y1, 1, radius)
        addIndexQuad()

        // south
        addVertex(x2, -1.0f, y2, 2, radius)
        addVertex(x2, +1.0f, y2, 3, radius)
        addVertex(x1, +1.0f, y2, 0, radius)
        addVertex(x1, -1.0f, y2, 1, radius)
        addIndexQuad()

        // west
        addVertex(x1, -1.0f, y2, 2, radius)
        addVertex(x1, +1.0f, y2, 3, radius)
        addVertex(x1, +1.0f, y1, 0, radius)
        addVertex(x1, -1.0f, y1, 1, radius)
        addIndexQuad()

        // east
        addVertex(x2, -1.0f, y1, 2, radius)
        addVertex(x2, +1.0f, y1, 3, radius)
        addVertex(x2, +1.0f, y2, 0, radius)
        addVertex(x2, -1.0f, y2, 1, radius)
        addIndexQuad()
    }

    private fun addVertex(x: Float, y: Float, z: Float, uvIndex: Int, width: Float) = data.add(
        x, y, z,
        uvIndex.buffer(),
        width,
    )

    override fun bake() = WorldBorderMesh(offset, center, radius, createVertexBuffer())


    data class WorldBorderMeshStruct(
        val position: Vec3f,
        val uvIndex: Int,
        val width: Float,
    ) {
        companion object : MeshStruct(WorldBorderMeshStruct::class)
    }
}
