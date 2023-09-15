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

package de.bixilon.minosoft.gui.rendering.chunk.border

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class WorldBorderMesh(
    context: RenderContext,
    val offset: Vec3i,
    val center: Vec2d,
    val radius: Double,
) : Mesh(context, WorldBorderMeshStruct, initialCacheSize = 6 * 2 * 3 * WorldBorderMeshStruct.FLOATS_PER_VERTEX) {


    private fun width(): Float {
        return minOf(radius.toFloat(), World.MAX_RENDER_DISTANCE.toFloat() * ProtocolDefinition.SECTION_WIDTH_X)
    }

    private fun positions(width: Float, center: Double): Array<Vec2> {
        val left = (center - width).toFloat()
        val right = (center + width).toFloat()

        return arrayOf(
            Vec2(left, -1.0f),
            Vec2(left, +1.0f),
            Vec2(right, +1.0f),
            Vec2(right, -1.0f),
        )
    }

    private fun textureIndex(index: Int): Int {
        return when (index) {
            1 -> 2
            2 -> 1
            3 -> 0
            else -> 3
        }
    }

    private fun addVertexX(x: Float, width: Float, positions: Array<Vec2>, rotated: Boolean) {
        for (index in 0 until order.size step 2) {
            val (z, y) = positions[index]
            val texture = if (rotated) textureIndex(index + 1) else index + 1
            addVertex(x, y, z, textureIndex(texture), width)
        }
    }

    private fun x(width: Float) {
        val positions = positions(width, center.y)
        addVertexX((maxOf(-WorldBorder.MAX_RADIUS, center.x - radius) - offset.x).toFloat(), width, positions, false)
        addVertexX((minOf(WorldBorder.MAX_RADIUS, center.x + radius) - offset.x).toFloat(), width, positions, true)
    }

    private fun addVertexZ(z: Float, width: Float, positions: Array<Vec2>, rotated: Boolean) {
        for (index in 0 until order.size step 2) {
            val (x, y) = positions[index]
            val texture = if (rotated) textureIndex(index + 1) else index + 1
            addVertex(x, y, z, textureIndex(texture), width)
        }
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
        data.add(x)
        data.add(y)
        data.add(z)
        data.add(uvIndex.buffer())
        data.add(width)
    }


    data class WorldBorderMeshStruct(
        val position: Vec3,
        val uvIndex: Int,
        val width: Float,
    ) {
        companion object : MeshStruct(WorldBorderMeshStruct::class)
    }
}
