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

package de.bixilon.minosoft.camera.target

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.camera.ConnectionCamera
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.camera.target.targets.FluidTarget
import de.bixilon.minosoft.camera.target.targets.GenericTarget
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.voxel.AABBRaycastHit
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.floor
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.raycastDistance

class TargetHandler(
    private val camera: ConnectionCamera,
) {
    var target: GenericTarget? by observed(null)
        private set
    var fluidTarget: FluidTarget? by observed(null)
        private set


    fun update() {
        val entity = camera.entity
        val position = entity.renderInfo.eyePosition.toVec3d
        val front = (if (entity is LocalPlayerEntity) entity.physics.rotation else entity.renderInfo.rotation).front.toVec3d

        val (target, fluid) = this.raycast(position, front)
        this.target = target
        this.fluidTarget = fluid
    }

    fun raycast(origin: Vec3d, front: Vec3d): Pair<GenericTarget?, FluidTarget?> {
        val (block: BlockTarget?, fluid) = raycastBlock(origin, front)
        val entity = raycastEntity(origin, front, block?.distance ?: Double.MAX_VALUE)

        val target = when {
            entity == null -> block
            block == null -> entity
            entity.distance < block.distance -> entity
            else -> block
        }
        return Pair(target, fluid)
    }

    private fun raycastEntity(origin: Vec3d, front: Vec3d, maxDistance: Double): EntityTarget? {
        var target: EntityTarget? = null

        val originF = Vec3(origin)
        val world = camera.connection.world

        world.entities.lock.acquire()
        for (entity in world.entities) {
            if (entity is LocalPlayerEntity) continue
            if (!entity.canRaycast) continue
            if ((entity.renderInfo.position - originF).length2() > MAX_ENTITY_DISTANCE) {
                continue
            }
            val aabb = entity.renderInfo.cameraAABB
            val (distance, direction) = aabb.raycast(origin, front) ?: continue

            if (distance > maxDistance) continue // other target is already closer

            if (target != null && target.distance <= distance) continue // previous target is closer

            target = EntityTarget(origin + front * distance, distance, direction, entity)
        }
        world.entities.lock.release()

        return target
    }

    private fun raycast(origin: Vec3d, front: Vec3d, state: BlockState, blockPosition: BlockPosition): AABBRaycastHit? {
        if (state.block !is OutlinedBlock) {
            // no block, continue going into that direction
            return null
        }
        var shape = state.block.getOutlineShape(camera.connection, state) ?: return null
        state.block.nullCast<RandomOffsetBlock>()?.offsetBlock(blockPosition)?.let { shape += it }

        shape += blockPosition

        return shape.raycast(origin, front)
    }

    fun raycastBlock(origin: Vec3d, front: Vec3d): Pair<BlockTarget?, FluidTarget?> {
        val position = Vec3d(origin)
        var chunk: Chunk? = null

        var fluid: FluidTarget? = null


        for (step in 0..MAX_STEPS) {
            if (step > 0) position += front * position.raycastDistance(front)// TODO 2 steps if diagonal

            val blockPosition = position.floor
            val chunkPosition = blockPosition.chunkPosition
            if (chunk == null) {
                chunk = camera.connection.world.chunks[chunkPosition] ?: break
            } else if (chunk.chunkPosition != chunkPosition) {
                chunk = chunk.neighbours.trace(chunkPosition - chunk.chunkPosition) ?: break
            }
            val state = chunk[blockPosition.inChunkPosition] ?: continue
            if (state.block is FluidBlock) {
                if (fluid == null) {
                    val hit = raycast(origin, front, state, blockPosition)
                    if (hit != null) {
                        fluid = FluidTarget(position + front * hit.distance, hit.distance, hit.direction, state, blockPosition, state.block.fluid)
                    }
                }
                continue
            }
            val hit = raycast(origin, front, state, blockPosition) ?: continue
            val entity = chunk.getBlockEntity(blockPosition.inChunkPosition)
            val target = BlockTarget(origin + front * hit.distance, hit.distance, hit.direction, state, entity, blockPosition, hit.inside)
            return Pair(target, fluid)
        }

        return Pair(null, fluid)
    }

    companion object {
        private const val MAX_STEPS = 100
        private const val MAX_ENTITY_DISTANCE = 30.0f * 30.0f // length2 is not doing square root
    }
}
