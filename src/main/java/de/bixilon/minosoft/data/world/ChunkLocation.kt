/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.Camera
import glm_.Java.Companion.glm
import glm_.vec2.Vec2
import kotlin.math.abs

data class ChunkLocation(val x: Int, val z: Int) {

    override fun toString(): String {
        return "($x $z)"
    }

    fun getLocationByDirection(direction: Directions): ChunkLocation {
        return when (direction) {
            Directions.NORTH -> ChunkLocation(x, z - 1)
            Directions.SOUTH -> ChunkLocation(x, z + 1)
            Directions.WEST -> ChunkLocation(x - 1, z)
            Directions.EAST -> ChunkLocation(x + 1, z)
            else -> throw IllegalArgumentException("Chunk location is just 2d")
        }
    }

    fun isVisibleFrom(camera: Camera): Boolean {
        val from = Vec2(x * 16, z * 16)
        val to = from + Vec2(16, 16)
        val frustrum: Frustrum
        // val origin = Vec2(camera.cameraPosition.x, camera.cameraPosition.z)
        // if (isInCone(from, origin, camera.yaw, camera.fov)) {
        //     return true
        // }
        // if (isInCone(to, origin, camera.yaw, camera.fov)) {
        //     return true
        // }
        // if (intersectsQuad(from, to, origin, -glm.radians(camera.yaw + camera.fov / 2))) {
        //     return true
        // }
        // if (intersectsQuad(from, to, origin, -glm.radians(camera.yaw - camera.fov / 2))) {
        //     return true
        // }
        // return false
    }

    private fun isInCone(point: Vec2, origin: Vec2, yaw: Double, fov: Float): Boolean {
        val difference = (point - origin).normalize()
        val angle = Math.toDegrees(glm.asin(difference.y).toDouble())
        val realYaw = if (yaw > 0) {
            yaw
        } else {
            yaw + 360
        }
        val realAngle = if (angle > 0) {
            angle
        } else {
            angle + 180
        }
        return abs(angle) < fov
    }

    private fun intersectsQuad(from: Vec2, to: Vec2, origin: Vec2, angle: Double): Boolean {
        val direction = Vec2(glm.cos(angle), glm.sin(angle))
        if (intersect(origin, direction, from, Vec2(from.x, to.y))) {
            return true
        }
        if (intersect(origin, direction, from, Vec2(to.x, from.y))) {
            return true
        }
        if (intersect(origin, direction, to, Vec2(from.x, to.y))) {
            return true
        }
        if (intersect(origin, direction, to, Vec2(to.x, from.y))) {
            return true
        }
        return false
    }

    private fun intersectLines(v1: Vec2, v2: Vec2, v3: Vec2, v4: Vec2): Vec2 {
        // formula from https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
        val d = (v1.x - v2.x) * (v3.y - v4.y) - (v1.y - v2.y) * (v3.x - v4.x)
        val x = (v1.x * v2.y - v1.y * v2.x) * (v3.x - v4.x) - (v1.x - v2.x) * (v3.x * v4.y - v3.y * v4.x)
        val y = (v1.x * v2.y - v1.y * v2.x) * (v4.x - v3.x) - (v1.y - v2.y) * (v3.x * v4.y - v3.y * v4.x)
        return Vec2(x / d, y / d)
    }

    private fun intersect(origin: Vec2, direction: Vec2, p1: Vec2, p2: Vec2): Boolean {
        val normal = Vec2(direction.yx)
        val first = dotProduct(normal, p1-origin)
        val second = dotProduct(normal, p2-origin)
        return (first.toBits() and 0x80000000.toInt() != second.toBits() and 0x80000000.toInt())
    }

    private fun dotProduct(v1: Vec2, v2: Vec2): Float {
        return v1.x * v2.x + v1.y * v2.y
    }
}
