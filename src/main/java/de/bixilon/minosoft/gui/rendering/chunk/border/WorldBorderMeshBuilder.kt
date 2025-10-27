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

package de.bixilon.minosoft.gui.rendering.chunk.border

import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadConsumer.Companion.iterate
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class WorldBorderMeshBuilder(
    context: RenderContext,
    val offset: BlockPosition,
    val center: Vec2d,
    val radius: Double,
) : QuadMeshBuilder(context, WorldBorderMeshStruct, 4) {

    private fun width(): Float {
        return minOf(radius.toFloat(), World.MAX_RENDER_DISTANCE.toFloat() * ChunkSize.SECTION_WIDTH_X)
    }

    private fun positions(width: Float, center: Double): Array<Vec2f> {
        val left = (center - width).toFloat()
        val right = (center + width).toFloat()

        return arrayOf(
            Vec2f(left, -1.0f),
            Vec2f(left, +1.0f),
            Vec2f(right, +1.0f),
            Vec2f(right, -1.0f),
        )
    }

    private fun textureIndex(index: Int) = when (index) {
        1 -> 2
        2 -> 1
        3 -> 0
        else -> 3
    }

    private fun addVertexX(x: Float, width: Float, positions: Array<Vec2f>, rotated: Boolean) {
        iterate {  // TODO: verify render order
            val (z, y) = positions[it]
            val texture = if (rotated) textureIndex(it + 1) else it + 1
            addVertex(x, y, z, textureIndex(texture), width)
        }
        addIndexQuad()
    }

    private fun x(width: Float) {
        val positions = positions(width, center.y)
        addVertexX((maxOf(-WorldBorder.MAX_RADIUS, center.x - radius) - offset.x).toFloat(), width, positions, false)
        addVertexX((minOf(WorldBorder.MAX_RADIUS, center.x + radius) - offset.x).toFloat(), width, positions, true)
    }

    private fun addVertexZ(z: Float, width: Float, positions: Array<Vec2f>, rotated: Boolean) {
        iterate { // TODO: verify render order
            val (x, y) = positions[it]
            val texture = if (rotated) textureIndex(it + 1) else it + 1
            addVertex(x, y, z, textureIndex(texture), width)
        }
        addIndexQuad()
    }

    private fun z(width: Float) {
        val positions = positions(width, center.x)

        addVertexZ((maxOf(-WorldBorder.MAX_RADIUS, center.y - radius) - offset.z).toFloat(), width, positions, true)
        addVertexZ((minOf(WorldBorder.MAX_RADIUS, center.y + radius) - offset.z).toFloat(), width, positions, false)
    }

    fun build() {
        val width = width()
        x(width)
        z(width)
    }

    private fun addVertex(x: Float, y: Float, z: Float, uvIndex: Int, width: Float) {
        data.add(
            x, y, z,
            uvIndex.buffer(),
            width,
        )
    }

    override fun bake() = WorldBorderMesh(offset, center, radius, create())


    data class WorldBorderMeshStruct(
        val position: Vec3f,
        val uvIndex: Int,
        val width: Float,
    ) {
        companion object : MeshStruct(WorldBorderMeshStruct::class)
    }
}
