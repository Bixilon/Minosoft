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

package de.bixilon.minosoft.gui.rendering.camera.target

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.FluidBlock
import de.bixilon.minosoft.data.registries.shapes.VoxelShape
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.FluidTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.GenericTarget
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.floor
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition

class TargetHandler(
    private val context: RenderContext,
    private var camera: Camera,
) {
    private val connection = context.connection

    /**
     * Can ba a BlockTarget or an EntityTarget. Not a FluidTarget
     */
    var target: GenericTarget? = null
        private set
    var fluidTarget: FluidTarget? = null
        private set


    fun raycast() {
        val eyePosition = camera.matrixHandler.entity.eyePosition.toVec3d
        val cameraFront = camera.matrixHandler.entity.rotation.front.toVec3d

        target = raycast(eyePosition, cameraFront, blocks = true, fluids = false, entities = true)
        fluidTarget = raycast(eyePosition, cameraFront, blocks = false, fluids = true, entities = false) as FluidTarget?
    }


    private fun raycastEntity(origin: Vec3d, direction: Vec3d): EntityTarget? {
        var currentHit: EntityTarget? = null

        val originF = Vec3(origin)
        connection.world.entities.lock.acquire()
        for (entity in connection.world.entities) {
            if (entity is LocalPlayerEntity) {
                continue
            }
            if ((entity.cameraPosition - originF).length2() > MAX_ENTITY_DISTANCE) {
                continue
            }
            val target = VoxelShape(entity.cameraAABB).raycast(origin, direction)
            if (!target.hit) {
                continue
            }
            if (currentHit != null && currentHit.distance < target.distance) {
                continue
            }
            currentHit = EntityTarget(origin + direction * target.distance, target.distance, target.direction, entity)
        }
        connection.world.entities.lock.release()
        return currentHit
    }

    fun raycast(origin: Vec3d, direction: Vec3d, blocks: Boolean, fluids: Boolean, entities: Boolean): GenericTarget? {
        if (!blocks && !fluids && entities) {
            // only raycast entities
            return raycastEntity(origin, direction)
        }
        val currentPosition = Vec3d(origin)

        fun getTotalDistance(): Double {
            return (origin - currentPosition).length()
        }

        var target: GenericTarget? = null
        var run = 0
        var currentChunk: Chunk? = null
        var currentChunkPosition = Vec2i.EMPTY
        for (i in 0..RAYCAST_MAX_STEPS) {
            val blockPosition = currentPosition.floor
            val chunkPosition = blockPosition.chunkPosition
            if (chunkPosition != currentChunkPosition || run++ == 0) {
                currentChunk = connection.world[chunkPosition]
                currentChunkPosition = chunkPosition
            }
            val blockState = currentChunk?.get(blockPosition.inChunkPosition)

            if (blockState == null) {
                currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxisInDirection(currentPosition, direction) + 0.001)
                continue
            }
            val voxelShapeRaycastResult = (blockState.block.getOutlineShape(connection, blockState, blockPosition) + blockPosition + blockPosition.getWorldOffset(blockState.block)).raycast(currentPosition, direction)
            if (voxelShapeRaycastResult.hit) {
                val distance = getTotalDistance()
                currentPosition += direction * voxelShapeRaycastResult.distance
                currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxisInDirection(currentPosition, direction) + 0.001)

                if (blockState.block is FluidBlock) {
                    if (!fluids) {
                        continue
                    }
                    target = FluidTarget(
                        currentPosition,
                        distance,
                        voxelShapeRaycastResult.direction,
                        blockState,
                        blockPosition,
                        blockState.block.fluid,
                    )
                    break
                }


                if (!blocks) {
                    continue
                }

                val entity = currentChunk?.getBlockEntity(blockPosition.inChunkPosition)

                target = BlockTarget(
                    currentPosition,
                    distance,
                    voxelShapeRaycastResult.direction,
                    blockState,
                    entity,
                    blockPosition,
                )
                break
            } else {
                currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxisInDirection(currentPosition, direction) + 0.001)
            }
        }

        if (entities) {
            val entityTarget = raycastEntity(origin, direction) ?: return target
            if (target == null) {
                return entityTarget
            }
            return (entityTarget.distance < target.distance).decide(entityTarget, target)
        }

        return target
    }

    companion object {
        private const val RAYCAST_MAX_STEPS = 100
        private const val MAX_ENTITY_DISTANCE = 30.0f * 30.0f // length2 does not get the square root
    }
}
