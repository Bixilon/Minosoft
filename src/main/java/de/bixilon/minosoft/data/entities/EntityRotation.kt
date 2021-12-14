/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities

import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.vec3.Vec3

data class EntityRotation(
    val yaw: Double,
    val pitch: Double,
) {
    val front: Vec3
        get() = Vec3(
            (yaw + 90).rad.cos * (-pitch).rad.cos,
            (-pitch).rad.sin,
            (yaw + 90).rad.sin * (-pitch).rad.cos
        ).normalize()

    constructor(bodyYaw: Float, pitch: Float) : this(bodyYaw.toDouble(), pitch.toDouble())

    override fun toString(): String {
        return "(yaw=$yaw, pitch=$pitch)"
    }

    companion object {
        val EMPTY = EntityRotation(0.0, 0.0)
    }
}
