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

package de.bixilon.minosoft.physics.parts

import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.collision.CollisionShape
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.vec.vec3.d.MVec3d
import de.bixilon.minosoft.physics.entities.EntityPhysics
import kotlin.math.abs

object CollisionMovementPhysics {

    fun EntityPhysics<*>.collectCollisions(movement: Vec3d, aabb: AABB, predicate: CollisionPredicate? = null): CollisionShape {
        return CollisionShape(this.entity.session.world, EntityCollisionContext(entity, this, aabb), aabb, movement, positionInfo.chunk, predicate)
    }

    private fun checkMovement(axis: Axes, originalValue: Double, offsetAABB: Boolean, aabb: AABB, collisions: Shape): Double {
        var value = originalValue
        if (value == 0.0 || abs(value) < 1.0E-7) {
            return 0.0
        }
        value = collisions.calculateMaxDistance(aabb, value, axis)
        if (offsetAABB && value != 0.0) {
            aabb.unsafePlus(axis, value)
        }
        return value
    }

    fun collide(movement: Vec3d, aabb: AABB, collisions: Shape): MVec3d {
        if (movement.length2() < 1.0E-7) return MVec3d.EMPTY

        val adjustedAABB = AABB(aabb)
        val adjusted = MVec3d(movement)

        adjusted.y = checkMovement(Axes.Y, adjusted.y, true, adjustedAABB, collisions)

        val zPriority = adjusted.z > adjusted.x

        if (zPriority) {
            adjusted.z = checkMovement(Axes.Z, adjusted.z, true, adjustedAABB, collisions)
        }

        adjusted.x = checkMovement(Axes.X, adjusted.x, true, adjustedAABB, collisions)

        if (!zPriority) {
            adjusted.z = checkMovement(Axes.Z, adjusted.z, false, adjustedAABB, collisions)
        }


        if (adjusted.length2() > movement.length2()) {
            return MVec3d.EMPTY
        }

        return adjusted
    }

    fun EntityPhysics<*>.collide(movement: Vec3d): Vec3d {
        val aabb = aabb
        if (aabb.min.y <= BlockPosition.MIN_Y || aabb.max.y >= BlockPosition.MAX_Y) return movement // TODO: also check movement

        val collisions = collectCollisions(movement, aabb)
        try {
            val collision = collide(movement, aabb, collisions)
            if (stepHeight <= 0.0) {
                return collision
            }

            return collideStepping(movement, collision, collisions)
        } finally {
            collisions.free()
        }
    }

    private fun EntityPhysics<*>.collideStepping(movement: Vec3d, collision: Vec3d, collisions: Shape): Vec3d {
        val collidedX = movement.x.matches(collision.x) // TODO: currently not collided
        val collidedY = movement.y.matches(collision.y)
        val collidedZ = movement.z.matches(collision.z)

        val grounded = this.onGround || collidedY && movement.y < 0.0

        if (!grounded || !(collidedX || collidedZ)) return collision

        val stepHeight = stepHeight.toDouble()

        var total = collide(Vec3d(movement.x, stepHeight, movement.z), aabb, collisions)
        val vertical = collide(Vec3d(0.0, stepHeight, 0.0), aabb.extend(Vec3d(movement.x, 0.0, movement.z)), collisions)

        if (vertical.y < stepHeight) {
            val horizontal = collide(Vec3d(movement.x, 0.0, movement.z), aabb.offset(vertical), collisions) + vertical
            if (horizontal.xz.length2() > total.length2()) {
                total = horizontal
            }
        }
        if (total.xz.length2() > collision.length2()) {
            return total + collide(Vec3d(0.0, -total.y + movement.y, 0.0), aabb.offset(total), collisions)
        }

        return collision
    }
}
