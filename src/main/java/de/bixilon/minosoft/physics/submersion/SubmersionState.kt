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

package de.bixilon.minosoft.physics.submersion

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.vehicle.boat.Boat
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.handler.FluidCollisionHandler
import de.bixilon.minosoft.data.registries.fluid.handler.FluidEnterHandler
import de.bixilon.minosoft.data.registries.fluid.handler.FluidLeaveHandler
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.VanillaMath.vanillaNormalizeAssign
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.physics.properties.SwimmingVehicle
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import kotlin.math.abs

class SubmersionState(private val physics: EntityPhysics<*>) : Tickable {
    private val world = physics.entity.connection.world
    var eye: Fluid? = null
        private set
    var heights: Object2DoubleOpenHashMap<Fluid> = Object2DoubleOpenHashMap()
        private set

    @Deprecated("eye is WaterFluid")
    var waterSubmersionState: Boolean = false
        private set

    var primaryFluid: Fluid? = null
        private set


    private fun getFluidHeight(position: Vec3i, state: BlockState, fluid: Fluid): Float {
        val top = world[position + Directions.UP]
        if (fluid.matches(top)) {
            return 1.0f
        }
        return fluid.getHeight(state)
    }

    private fun getFluidUpdate(fluid: Fluid, aabb: AABB, pushable: Boolean): FluidUpdate? {
        var totalHeight = 0.0
        val totalVelocity = Vec3d.EMPTY
        var count = 0

        for ((position, state, chunk) in WorldIterator(aabb.positions(), world, physics.positionInfo.chunk)) {
            if (!fluid.matches(state)) continue // TODO: tags?

            val height = position.y + if (fluid.matches(chunk[position.inChunkPosition + Directions.UP])) 1.0f else fluid.getHeight(state)
            if (height < aabb.min.y) continue

            count++
            totalHeight = maxOf(totalHeight, height - aabb.min.y)
            if (!pushable) continue

            val velocity = fluid.getVelocity(state, position, chunk)
            if (totalHeight < 0.4) {
                velocity *= totalHeight
            }
            totalVelocity += velocity
        }
        if (count == 0) return null

        return FluidUpdate(totalHeight, totalVelocity, count)
    }

    private fun updateVelocity(fluid: Fluid, update: FluidUpdate, normalize: Boolean) {
        if (update.velocity.length2() <= 0.0) return
        val speed = fluid.getVelocityMultiplier(physics.entity.connection)
        update.velocity *= 1.0 / update.count
        if (normalize) {
            update.velocity.vanillaNormalizeAssign()
        }
        update.velocity *= speed

        if (abs(physics.velocity.x) < Vec3dUtil.MARGIN && abs(physics.velocity.z) < Vec3dUtil.MARGIN && update.velocity.length2() < (FLUID_SPEED * FLUID_SPEED)) {
            update.velocity.vanillaNormalizeAssign()
            update.velocity *= FLUID_SPEED
        }
        physics.velocity = physics.velocity + update.velocity
    }

    private fun update(fluid: Fluid?, aabb: AABB, pushable: Boolean, previousHeight: Double) {
        if (fluid == null) return
        val update = getFluidUpdate(fluid, aabb, pushable)

        if (update == null) {
            if (fluid is FluidLeaveHandler) fluid.onLeave(physics)
            return
        }
        updateVelocity(fluid, update, physics.entity !is PlayerEntity)

        heights[fluid] = update.height
        val primary = this.primaryFluid
        if (primary == null || fluid.priority < primary.priority) {
            this.primaryFluid = fluid
        }

        if (previousHeight > 0.0) {
            // already in
            if (fluid is FluidCollisionHandler) fluid.onCollision(physics, update.height)
        } else {
            if (fluid is FluidEnterHandler) fluid.onEnter(physics, update.height)
        }
    }

    @Deprecated("performance")
    private fun update(type: ResourceLocation, aabb: AABB, pushable: Boolean, previousHeight: Double) {
        val fluid = physics.entity.connection.registries.fluid[type] // TODO: remove this and stream fluids: waterlogged makes problems
        update(fluid, aabb, pushable, previousHeight)
    }

    @Deprecated("performance")
    private fun update(type: Identified, aabb: AABB, pushable: Boolean, previous: Double) = update(type.identifier, aabb, pushable, previous)

    private fun updateWaterSubmersion() {
        waterSubmersionState = eye is WaterFluid
        eye = null
        val eyeHeight = physics.eyeY - Fluid.MIN_LEVEL

        val vehicle = physics.entity.attachment.vehicle
        if (vehicle is Boat) {
            // TODO
        }
        val position = physics.position
        val block = physics.positionInfo.chunk?.get(position.x.toInt() and 0x0F, position.y.toInt(), position.z.toInt() and 0x0F) ?: return
        if (block.block !is FluidHolder) {
            return
        }
        val eyePosition = Vec3i(physics.position.x.toInt(), eyeHeight.toInt(), physics.position.z.toInt())

        val fluidHeight = eyePosition.y + getFluidHeight(eyePosition, block, block.block.fluid)
        if (fluidHeight > eyeHeight) {
            eye = block.block.fluid
        }
    }


    fun updateWater(aabb: AABB = physics.aabb.shrink(0.001), pushable: Boolean = physics.fluidPushable, previous: Double = heights.getDouble(WaterFluid)) {
        val vehicle = physics.entity.attachment.vehicle
        if (vehicle is SwimmingVehicle && !vehicle.canUpdatePassengerFluidMovement(WaterFluid)) {
            return
        }
        update(physics.entity.connection.registries.fluid.water, aabb, pushable, previous)
    }

    private fun clear() {
        this.heights = Object2DoubleOpenHashMap(0)
        primaryFluid = null
    }

    private fun update() {
        val previous = this.heights
        clear()

        val aabb = physics.aabb.shrink(0.001)
        val pushable = physics.fluidPushable

        updateWater(aabb, pushable, previous.getDouble(WaterFluid))
        update(physics.entity.connection.registries.fluid.lava, aabb, pushable, previous.getDouble(LavaFluid))
    }

    override fun tick() {
        update()
        updateWaterSubmersion()
    }

    operator fun get(fluid: Fluid?) = this.heights.getDouble(fluid)
    operator fun get(fluid: ResourceLocation) = this.heights.getDouble(fluid) // that works because the fluid is using the identifier as hashCode and equals
    operator fun get(fluid: Identified) = this[fluid.identifier]

    private companion object {
        const val FLUID_SPEED = 0.0045000000000000005
    }
}
