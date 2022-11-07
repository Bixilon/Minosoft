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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class CloudMesh(renderWindow: RenderWindow) : Mesh(renderWindow, CloudMeshStruct, renderWindow.renderSystem.preferredPrimitiveType) {

    fun addVertex(start: Vec3, side: Directions) {
        data.addAll(start.array)
        data.add(side.ordinal.buffer())
    }


    fun createCloud(start: Vec2i, end: Vec2i, yOffset: Int, height: Float, culling: BooleanArray) {
        val start = Vec3(start.x, yOffset, start.y)
        val end = Vec3(end.x, yOffset + height, end.y)

        addYQuad(Vec2(start.x, start.z), end.y, Vec2(end.x, end.z)) { position, _ -> addVertex(position, Directions.UP) }
        addYQuad(Vec2(start.x, start.z), start.y, Vec2(end.x, end.z)) { position, _ -> addVertex(position, Directions.DOWN) }


        if (!culling[Directions.O_NORTH - Directions.SIDE_OFFSET]) {
            addZQuad(Vec2(start.x, start.y), start.z, Vec2(end.x, end.y)) { position, _ -> addVertex(position, Directions.NORTH) }
        }
        if (!culling[Directions.O_SOUTH - Directions.SIDE_OFFSET]) {
            addZQuad(Vec2(start.x, start.y), end.z, Vec2(end.x, end.y)) { position, _ -> addVertex(position, Directions.SOUTH) }
        }

        if (!culling[Directions.O_WEST - Directions.SIDE_OFFSET]) {
            addXQuad(Vec2(start.y, start.z), start.x, Vec2(end.y, end.z)) { position, _ -> addVertex(position, Directions.WEST) }
        }
        if (!culling[Directions.O_EAST - Directions.SIDE_OFFSET]) {
            addXQuad(Vec2(start.y, start.z), end.x, Vec2(end.y, end.z)) { position, _ -> addVertex(position, Directions.EAST) }
        }
    }

    data class CloudMeshStruct(
        val position: Vec3,
        val side: Int,
    ) {
        companion object : MeshStruct(CloudMeshStruct::class)
    }
}
