package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.EntityRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.FluidRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.floor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec3.Vec3d

class RaycastHandler(
    private val renderWindow: RenderWindow,
    private var camera: Camera,
) {
    private val connection = renderWindow.connection

    // ToDo: They should also be available in headless mode
    var nonFluidTarget: RaycastHit? = null
        private set
    var target: RaycastHit? = null
        private set
    var blockTarget: BlockRaycastHit? = null // Block target or if blocked by entity null
        private set
    var fluidTarget: FluidRaycastHit? = null
        private set
    var entityTarget: EntityRaycastHit? = null
        private set


    fun raycast() {
        // ToDo
        val eyePosition = camera.matrixHandler.eyePosition.toVec3d
        val cameraFront = camera.matrixHandler.cameraFront.toVec3d

        target = raycast(eyePosition, cameraFront, blocks = true, fluids = true, entities = true)
        nonFluidTarget = raycast(eyePosition, cameraFront, blocks = true, fluids = false, entities = true)
        blockTarget = raycast(eyePosition, cameraFront, blocks = true, fluids = false, entities = false) as BlockRaycastHit?
        fluidTarget = raycast(eyePosition, cameraFront, blocks = false, fluids = true, entities = false) as FluidRaycastHit?
        entityTarget = raycast(eyePosition, cameraFront, blocks = false, fluids = false, entities = true) as EntityRaycastHit?
    }


    private fun raycastEntity(origin: Vec3d, direction: Vec3d): EntityRaycastHit? {
        var currentHit: EntityRaycastHit? = null

        for (entity in connection.world.entities) {
            if (entity is LocalPlayerEntity) {
                continue
            }
            val hit = VoxelShape(entity.cameraAABB).raycast(origin, direction)
            if (!hit.hit) {
                continue
            }
            if ((currentHit?.distance ?: Double.MAX_VALUE) < hit.distance) {
                continue
            }
            currentHit = EntityRaycastHit(origin + direction * hit.distance, hit.distance, hit.direction, entity)

        }
        return currentHit
    }

    private fun raycast(origin: Vec3d, direction: Vec3d, blocks: Boolean, fluids: Boolean, entities: Boolean): RaycastHit? {
        if (!blocks && !fluids && entities) {
            // only raycast entities
            return raycastEntity(origin, direction)
        }
        val currentPosition = Vec3d(origin)

        fun getTotalDistance(): Double {
            return (origin - currentPosition).length()
        }

        var hit: RaycastHit? = null
        for (i in 0..RAYCAST_MAX_STEPS) {
            val blockPosition = currentPosition.floor
            val blockState = connection.world[blockPosition]

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
                    hit = FluidRaycastHit(
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
                hit = BlockRaycastHit(
                    currentPosition,
                    distance,
                    voxelShapeRaycastResult.direction,
                    blockState,
                    blockPosition,
                )
                break
            } else {
                currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxisInDirection(currentPosition, direction) + 0.001)
            }
        }

        if (entities) {
            val entityRaycastHit = raycastEntity(origin, direction) ?: return hit
            hit ?: return null
            return (entityRaycastHit.distance < hit.distance).decide(entityRaycastHit, hit)
        }

        return hit
    }

    companion object {
        private const val RAYCAST_MAX_STEPS = 100
    }
}
