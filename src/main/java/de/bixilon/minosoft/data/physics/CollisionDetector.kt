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
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

class CollisionDetector(val connection: PlayConnection) {

    fun getCollisionsToCheck(deltaPosition: Vec3, aabb: AABB, ignoreUnloadedChunks: Boolean = true): VoxelShape {
        // also look at blocks further down to also cover blocks with a higher than normal hitbox (for example fences)
        val blockPositions = (aabb extend deltaPosition extend Directions.DOWN.directionVector).getBlockPositions()
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

    fun collide(entity: Entity?, deltaPosition: Vec3, collisionsToCheck: VoxelShape, aabb: AABB): Vec3 {
        val delta = Vec3(deltaPosition)
        if (delta.y != 0.0f) {
            delta.y = collisionsToCheck.computeOffset(aabb, deltaPosition.y, Axes.Y)
            if (delta.y != deltaPosition.y) {
                entity?.let {
                    it.onGround = false
                    it.velocity.y = 0.0f
                    if (deltaPosition.y < 0) {
                        it.onGround = true
                    }
                }
                aabb += Vec3(0f, delta.y, 0f)
            } else if (delta.y < 0) {
                entity?.let { it.onGround = false }
            }
        }
        if ((deltaPosition.x != 0f || deltaPosition.z != 0f)) {
            val testDelta = Vec3(delta)
            testDelta.y = Entity.STEP_HEIGHT
            val stepMovementY = collisionsToCheck.computeOffset(aabb + testDelta, -Entity.STEP_HEIGHT, Axes.Y)
            if (stepMovementY < 0 && stepMovementY >= -Entity.STEP_HEIGHT) {
                testDelta.y = Entity.STEP_HEIGHT + stepMovementY
                aabb += Vec3(0f, testDelta.y, 0f)
                delta.y += testDelta.y
            }
        }
        val xPriority = delta.x > delta.z
        if (delta.x != 0.0f && xPriority) {
            delta.x = collisionsToCheck.computeOffset(aabb, deltaPosition.x, Axes.X)
            aabb += Vec3(delta.x, 0f, 0f)
            if (delta.x != deltaPosition.x) {
                entity?.let { it.velocity.x = 0.0f }
            }
        }
        if (delta.z != 0.0f) {
            delta.z = collisionsToCheck.computeOffset(aabb, deltaPosition.z, Axes.Z)
            aabb += Vec3(0f, 0f, delta.z)
            if (delta.z != deltaPosition.z) {
                entity?.let { it.velocity.z = 0.0f }
            }
        }
        if (delta.x != 0.0f && !xPriority) {
            delta.x = collisionsToCheck.computeOffset(aabb, deltaPosition.x, Axes.X)
            // no need to offset the aabb any more, as it won't be used any more
            if (delta.x != deltaPosition.x) {
                entity?.let { it.velocity.x = 0.0f }
            }
        }
        if (delta.length() > deltaPosition.length() + Entity.STEP_HEIGHT) {
            return Vec3() // abort all movement if the collision system would move the entity further than wanted
        }
        return delta
    }
}
