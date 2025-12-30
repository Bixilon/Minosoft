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

package de.bixilon.minosoft.gui.rendering.sky.clouds.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.sky.clouds.generator.CloudGenerator
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class CloudMeshBuilder(
    context: RenderContext,
    generator: CloudGenerator,
    val offset: Vec2i,
    val style: CloudStyle,
) : QuadMeshBuilder(context, CloudMeshStruct) {

    fun addVertex(x: Float, y: Float, z: Float, side: Directions) = data.add(
        x, y, z,
        side.ordinal.buffer(),
    )

    fun addVertex(position: Vec3f, side: Directions) = addVertex(position.x, position.y, position.z, side)

    init {
        /*
        for (z in 0 until MESH_SIZE) {
            for (x in 0 until MESH_SIZE) {
                val realX = offset.x + x
                val realZ = offset.y + z

                if (!generator[realX, realZ]) continue

                val start = (this.offset * ARRAY_SIZE + Vec2i(x, z) + (layer.index * ARRAY_SIZE)) * CLOUD_SIZE

                val cull = booleanArrayOf(
                    generator[realX + 0, realZ - 1], // NORTH
                    generator[realX + 0, realZ + 1], // SOUTH
                    generator[realX - 1, realZ + 0], // WEST
                    generator[realX + 1, realZ + 0], // EAST
                )
                createCloud(start, start + CLOUD_SIZE, offset, layer.height, layer.height + CLOUD_HEIGHT, layer.clouds.flat, cull)
            }
        }

         */
    }


    private fun add(start: Vec2i, end: Vec2i, offset: BlockPosition, yStart: Int, yEnd: Int, culling: BooleanArray) {
        val start = Vec3f(start.x - offset.x, yStart - offset.y, start.y - offset.z) + CLOUD_OFFSET
        val end = Vec3f(end.x - offset.x, yEnd - offset.y, end.y - offset.z) + CLOUD_OFFSET

        addYQuad(Vec2f(start.x, start.z), start.y, Vec2f(end.x, end.z)) { position, _ -> addVertex(position, Directions.DOWN) }
        if (style == CloudStyle.VOLUME) {
            addYQuad(Vec2f(start.x, start.z), end.y, Vec2f(end.x, end.z)) { position, _ -> addVertex(position, Directions.UP) }


            if (!culling[Directions.O_NORTH - Directions.SIDE_OFFSET]) {
                addZQuad(Vec2f(start.x, start.y), start.z, Vec2f(end.x, end.y)) { position, _ -> addVertex(position, Directions.NORTH) }
            }
            if (!culling[Directions.O_SOUTH - Directions.SIDE_OFFSET]) {
                addZQuad(Vec2f(start.x, start.y), end.z, Vec2f(end.x, end.y)) { position, _ -> addVertex(position, Directions.SOUTH) }
            }

            if (!culling[Directions.O_WEST - Directions.SIDE_OFFSET]) {
                addXQuad(Vec2f(start.y, start.z), start.x, Vec2f(end.y, end.z)) { position, _ -> addVertex(position, Directions.WEST) }
            }
            if (!culling[Directions.O_EAST - Directions.SIDE_OFFSET]) {
                addXQuad(Vec2f(start.y, start.z), end.x, Vec2f(end.y, end.z)) { position, _ -> addVertex(position, Directions.EAST) }
            }
        }
    }

    data class CloudMeshStruct(
        val position: Vec3f,
        val side: Int,
    ) {
        companion object : MeshStruct(CloudMeshStruct::class)
    }

    companion object {
        const val CLOUD_HEIGHT = 4
        const val CLOUD_OFFSET = 0.24f // prevents face fighting with pretty much all blocks

        const val CLOUD_SIZE = 12
        const val MESH_SIZE = CLOUD_SIZE * 16
    }
}
