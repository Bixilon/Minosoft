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

package de.bixilon.minosoft.physics.handlers.movement

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.set
import de.bixilon.minosoft.physics.handlers.general.AbstractEntityPhysics

interface SneakAdjuster : StepAdjuster, AbstractEntityPhysics {

    fun shouldAdjustForSneaking(movement: Vec3d): Boolean

    private fun checkValue(value: Double): Double {
        return when {
            value < SNEAK_CHECK && value >= -SNEAK_CHECK -> 0.0
            value > 0.0 -> value - SNEAK_CHECK
            else -> value + SNEAK_CHECK
        }
    }

    private fun Entity.isSpaceEmpty(aabb: AABB, offset: Vec3d): Boolean {
        return connection.world.isSpaceEmpty(this, aabb.offset(offset), positionInfo.chunk)
    }

    private fun checkAxis(entity: Entity, value: Double, aabb: AABB, axis: Axes): Double {
        var value = value
        while (value != 0.0 && entity.isSpaceEmpty(aabb, Vec3d(0.0, -stepHeight, 0.0).apply { this[axis] = value })) {
            value = checkValue(value)
        }

        return value
    }

    fun adjustMovementForSneaking(movement: Vec3d): Vec3d {
        if (!shouldAdjustForSneaking(movement)) {
            return movement
        }
        return adjustMovementForSneaking(_entity, aabb, movement)
    }

    fun adjustMovementForSneaking(entity: Entity, aabb: AABB, movement: Vec3d): Vec3d {
        var x = movement.x
        var z = movement.z

        x = checkAxis(entity, x, aabb, Axes.X)

        z = checkAxis(entity, z, aabb, Axes.Z)

        while (x != 0.0 && z != 0.0 && entity.isSpaceEmpty(aabb, Vec3d(Vec3d(x, -stepHeight, z)))) {
            x = checkValue(x)
            z = checkValue(z)
        }

        return Vec3d(x, movement.y, z)
    }

    companion object {
        const val SNEAK_CHECK = 0.05
    }
}
