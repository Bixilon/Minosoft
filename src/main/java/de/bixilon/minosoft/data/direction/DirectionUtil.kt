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

package de.bixilon.minosoft.data.direction

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3

object DirectionUtil {

    fun rotateMatrix(direction: Directions): Mat4 {
        return when (direction) {
            Directions.DOWN -> Mat4().translateAssign(Vec3(0.5f)).rotateAssign(180.0f.rad, Vec3(1, 0, 0)).translateAssign(Vec3(-0.5f))
            Directions.UP -> Mat4().translateAssign(Vec3(0.5f)).rotateAssign((-180.0f).rad, Vec3(1, 0, 0)).translateAssign(Vec3(-0.5f)) // ToDo
            Directions.NORTH -> Mat4()
            Directions.SOUTH -> Mat4().translateAssign(Vec3(0.5f)).rotateAssign(180.0f.rad, Vec3(0, 1, 0)).translateAssign(Vec3(-0.5f))
            Directions.WEST -> Mat4().translateAssign(Vec3(0.5f)).rotateAssign((-270.0f).rad, Vec3(0, 1, 0)).translateAssign(Vec3(-0.5f))
            Directions.EAST -> Mat4().translateAssign(Vec3(0.5f)).rotateAssign((-90.0f).rad, Vec3(0, 1, 0)).translateAssign(Vec3(-0.5f))
        }
    }
}
