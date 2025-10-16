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

package de.bixilon.minosoft.gui.rendering.models.util

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV

object CuboidUtil {

    fun positions(direction: Directions, from: Vec3f, to: Vec3f): FaceVertexData {
        return when (direction) {
            // @formatter:off
            Directions.DOWN ->  floatArrayOf(from.x, from.y, from.z,   from.x, from.y, to  .z,   to  .x, from.y, to  .z,   to  .x, from.y, from.z)
            Directions.UP ->    floatArrayOf(from.x, to  .y, from.z,   to  .x, to  .y, from.z,   to  .x, to  .y, to  .z,   from.x, to  .y, to  .z)
            Directions.NORTH -> floatArrayOf(from.x, from.y, from.z,   to  .x, from.y, from.z,   to  .x, to  .y, from.z,   from.x, to  .y, from.z)
            Directions.SOUTH -> floatArrayOf(from.x, from.y, to  .z,   from.x, to  .y, to  .z,   to  .x, to  .y, to  .z,   to  .x, from.y, to  .z)
            Directions.WEST ->  floatArrayOf(from.x, from.y, from.z,   from.x, to  .y, from.z,   from.x, to  .y, to  .z,   from.x, from.y, to  .z)
            Directions.EAST ->  floatArrayOf(to  .x, from.y, from.z,   to  .x, from.y, to  .z,   to  .x, to  .y, to  .z,   to  .x, to  .y, from.z)
            // @formatter:on
        }
    }

    fun cubeUV(offset: Vec2i, from: Vec3f, to: Vec3f, direction: Directions): FaceUV {
        val cube = MVec3f(to - from)

        val uv = MVec2f(offset)
        val size = when (direction.axis) {
            Axes.Y -> MVec2f(cube.x, cube.z)
            Axes.Z -> MVec2f(cube.x, cube.y)
            Axes.X -> MVec2f(cube.z, cube.y)
        }

        when (direction) {
            Directions.DOWN -> {
                uv.x += cube.z + cube.x
                // flip y coordinate
                uv.y += cube.z
                size.y = -cube.z
            }

            Directions.UP -> {
                uv.x += cube.z
            }

            Directions.NORTH -> {
                uv.x += cube.z + cube.x + cube.z
                uv.y += cube.z
            }

            Directions.SOUTH -> {
                uv.x += cube.z
                uv.y += cube.z
            }

            Directions.EAST -> {
                uv.x += cube.z + cube.x
                uv.y += cube.z
            }

            Directions.WEST -> {
                uv.y += cube.z
            }
        }


        return FaceUV(uv.unsafe, Vec2f(uv.x + size.x, uv.y + size.y))
    }
}
