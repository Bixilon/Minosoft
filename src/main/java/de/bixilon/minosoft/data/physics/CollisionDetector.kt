/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.physics

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import glm_.vec3.Vec3bool
import glm_.vec3.Vec3d
import kotlin.math.abs

class CollisionDetector(val connection: PlayConnection) {

    private fun getCollisionsToCheck(deltaPosition: Vec3d, aabb: AABB, ignoreUnloadedChunks: Boolean = true): VoxelShape {
        // also look at blocks further down to also cover blocks with a higher than normal hitbox (for example fences)
        val blockPositions = (aabb extend deltaPosition extend Directions.DOWN).blockPositions
        val result = VoxelShape()
        for (blockPosition in blockPositions) {
            val chunk = connection.world[blockPosition.chunkPosition]
            if ((chunk == null || !chunk.isFullyLoaded) && !ignoreUnloadedChunks) {
                // chunk is not loaded
                result.add(VoxelShape.FULL + blockPosition)
                continue
            }
            val blockState = chunk?.get(blockPosition.inChunkPosition) ?: continue
            result.add(blockState.collisionShape + blockPosition)
        }
        return result
    }

    fun sneak(entity: LocalPlayerEntity, deltaPosition: Vec3d): Vec3d {
        if (entity.baseAbilities.isFlying || !entity.isSneaking || !entity.canSneak()) {
            return deltaPosition
        }

        val movement = Vec3d(deltaPosition)

        fun checkValue(original: Double): Double {
            var value = original
            if (value < PhysicsConstants.SNEAK_MOVEMENT_CHECK && value >= -PhysicsConstants.SNEAK_MOVEMENT_CHECK) {
                value = 0.0
            } else if (value > 0.0) {
                value -= PhysicsConstants.SNEAK_MOVEMENT_CHECK
            } else {
                value += PhysicsConstants.SNEAK_MOVEMENT_CHECK
            }
            return value
        }

        fun checkAxis(original: Double, offsetMethod: (Double) -> Vec3d): Double {
            var value = original
            while (value != 0.0 && connection.world.isSpaceEmpty(entity.aabb + offsetMethod(value))) {
                value = checkValue(value)
            }
            return value
        }

        movement.x = checkAxis(movement.x) { Vec3d(it, -PhysicsConstants.STEP_HEIGHT, 0.0) }
        movement.z = checkAxis(movement.z) { Vec3d(0.0, -PhysicsConstants.STEP_HEIGHT, it) }


        while (movement.x != 0.0 && movement.z != 0.0 && connection.world.isSpaceEmpty(entity.aabb + Vec3(movement.x, -PhysicsConstants.STEP_HEIGHT, movement.z))) {
            movement.x = checkValue(movement.x)
            movement.z = checkValue(movement.z)
        }


        return movement
    }

    fun collide(physicsEntity: PhysicsEntity?, movement: Vec3d, aabb: AABB, stepping: Boolean = false): Vec3d {
        val collisionMovement = adjustMovementForCollisions(movement, aabb)

        var returnMovement = collisionMovement

        var blocked = Vec3bool(false)
        var onGround = false

        fun checkBlocked() {
            blocked = Vec3bool(returnMovement.x != movement.x, returnMovement.y != movement.y, returnMovement.z != movement.z)
            onGround = blocked.y && movement.y < 0.0
        }
        checkBlocked()

        if (stepping && onGround && (blocked.x || blocked.z)) {
            var stepMovement = adjustMovementForCollisions(Vec3d(movement.x, PhysicsConstants.STEP_HEIGHT, movement.z), aabb)
            val verticalStepMovement = adjustMovementForCollisions(Vec3d(0.0, PhysicsConstants.STEP_HEIGHT, 0.0), aabb extend Vec3d(movement.x, 0.0, movement.z))

            if (verticalStepMovement.y < PhysicsConstants.STEP_HEIGHT) {
                val horizontalStepMovement = adjustMovementForCollisions(Vec3d(movement.x, 0.0, movement.z), aabb + verticalStepMovement) + verticalStepMovement

                if (horizontalStepMovement.length() > stepMovement.length()) {
                    stepMovement = horizontalStepMovement
                }
            }
            if (stepMovement.length() > collisionMovement.length()) {
                returnMovement = stepMovement + adjustMovementForCollisions(Vec3d(0.0, -stepMovement.y + movement.y, 0.0), aabb + stepMovement)
            }
        }
        checkBlocked()
        physicsEntity?.let {
            if (blocked.x) {
                it.velocity.x = 0.0
            }
            if (blocked.y) {
                it.velocity.y = 0.0
            }
            it.onGround = onGround
            if (blocked.z) {
                it.velocity.z = 0.0
            }
        }
        return returnMovement
    }

    private fun adjustMovementForCollisions(movement: Vec3d, aabb: AABB, collisions: VoxelShape = connection.collisionDetector.getCollisionsToCheck(movement, aabb)): Vec3d {
        val adjustedAabb = AABB(aabb)
        val adjusted = Vec3d(movement)

        fun checkMovement(axis: Axes, originalValue: Double, offsetAABB: Boolean, offsetMethod: (Double) -> Vec3d): Double {
            var value = originalValue
            if (value == 0.0 || abs(value) < 1.0E-7) {
                return 0.0
            }
            value = collisions.computeOffset(adjustedAabb, value, axis)
            if (offsetAABB && value != 0.0) {
                adjustedAabb += offsetMethod(value)
            }
            return value
        }

        adjusted.y = checkMovement(Axes.Y, adjusted.y, true) { Vec3d(0.0f, it, 0.0f) }

        val zPriority = adjusted.z > adjusted.x

        if (zPriority) {
            adjusted.z = checkMovement(Axes.Z, adjusted.z, true) { Vec3d(0.0f, 0.0f, it) }
        }

        adjusted.x = checkMovement(Axes.X, adjusted.x, true) { Vec3d(it, 0.0f, 0.0f) }

        if (!zPriority) {
            adjusted.z = checkMovement(Axes.Z, adjusted.z, false) { Vec3d(0.0f, 0.0f, 0.0f) }
        }


        if (adjusted.length() > movement.length()) {
            return Vec3d.EMPTY // abort all movement if the collision system would move the entity further than wanted
        }

        return adjusted
    }
}
