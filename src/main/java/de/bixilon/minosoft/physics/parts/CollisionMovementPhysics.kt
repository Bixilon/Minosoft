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

package de.bixilon.minosoft.physics.parts

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.fixed.FixedCollidable
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.physics.entities.EntityPhysics
import kotlin.math.abs

object CollisionMovementPhysics {

    fun World.collectCollisions(context: CollisionContext, movement: Vec3d, aabb: AABB, chunk: Chunk?, predicate: CollisionPredicate? = null): VoxelShape {
        val shapes: ArrayList<AABB> = ArrayList()
        // TODO: add entity collisions (boat, shulker)
        // TODO: add world border collision shape

        val inChunk = Vec3i.EMPTY

        for ((position, state, chunk) in WorldIterator(aabb.extend(movement).grow(1.0).positions(), this, chunk)) {
            if (state.block !is CollidableBlock) continue

            if (predicate != null && !predicate.invoke(state)) continue
            // TODO: filter blocks (e.g. moving piston), whatever that means

            var shape = when (state.block) {
                is FixedCollidable -> state.block.getCollisionShape(state)
                is BlockWithEntity<*> -> {
                    inChunk.x = position.x and 0x0F
                    inChunk.z = position.z and 0x0F

                    state.block.getCollisionShape(context, position, state, chunk.getBlockEntity(inChunk))
                }

                else -> {
                    state.block.getCollisionShape(context, position, state, null)
                }
            } ?: continue
            shape += position

            if (position in aabb && shape.intersect(aabb)) {
                continue
            }
            shapes += shape
        }

        return VoxelShape(shapes)
    }

    fun EntityPhysics<*>.collectCollisions(movement: Vec3d, aabb: AABB, predicate: CollisionPredicate? = null): VoxelShape {
        return this.entity.connection.world.collectCollisions(EntityCollisionContext(entity, this, aabb), movement, aabb, positionInfo.chunk, predicate)
    }

    private fun checkMovement(axis: Axes, originalValue: Double, offsetAABB: Boolean, aabb: AABB, collisions: AbstractVoxelShape): Double {
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

    fun collide(movement: Vec3d, aabb: AABB, collisions: AbstractVoxelShape): Vec3d {
        if (movement.length2() < 1.0E-7) return movement

        val adjustedAABB = AABB(aabb)
        val adjusted = Vec3d(movement)

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
            return Vec3d.EMPTY // movement exceeds expected, some value gets invalid
        }

        return adjusted
    }

    fun EntityPhysics<*>.collide(movement: Vec3d): Vec3d {
        val aabb = aabb
        val collisions = collectCollisions(movement, aabb)
        val collision = collide(movement, aabb, collisions)
        if (stepHeight <= 0.0) {
            return collision
        }

        return collideStepping(movement, collision, collisions)
    }

    private fun EntityPhysics<*>.collideStepping(movement: Vec3d, collision: Vec3d, collisions: AbstractVoxelShape): Vec3d {
        val collided = movement.equal(collision)

        val grounded = this.onGround || collided.y && movement.y < 0.0

        if (!grounded || !(collided.x || collided.z)) return collision

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
