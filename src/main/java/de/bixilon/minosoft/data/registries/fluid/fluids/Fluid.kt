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
package de.bixilon.minosoft.data.registries.fluid.fluids

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFilled
import de.bixilon.minosoft.data.registries.blocks.types.FluidHolder
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.unbaked.fluid.FluidModel
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*
import kotlin.math.abs

open class Fluid(override val identifier: ResourceLocation) : RegistryItem() {
    open var model: FluidModel? = null

    override fun toString(): String {
        return identifier.full
    }

    open fun matches(other: Fluid): Boolean {
        return other == this
    }

    open fun matches(other: BlockState?): Boolean {
        other ?: return false
        if (other.block !is FluidHolder) {
            return false
        }

        return matches(other.block.fluid)
    }

    open fun getHeight(state: BlockState): Float {
        if (state.block is FluidFilled && state.block.fluid == this) {
            return 0.9f
        }
        if (state.block !is FluidBlock || state.block.fluid != this) {
            return 0.0f
        }
        val level = state.properties[BlockProperties.FLUID_LEVEL]?.toInt() ?: return 0.0f
        if (level < 0 || level >= 8) {
            return 0.9f
        }
        return (8 - level) / 9.0f
    }

    open fun travel(entity: LocalPlayerEntity, sidewaysSpeed: Float, forwardSpeed: Float, gravity: Double, falling: Boolean) {
        entity.accelerate(sidewaysSpeed, forwardSpeed, 0.02)
    }

    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) = Unit


    protected fun updateMovement(entity: Entity, gravity: Double, falling: Boolean, velocity: Vec3d): Vec3d {
        if (entity.hasGravity && !entity.isSprinting) {
            velocity.y = if (falling && abs(velocity.y - 0.005) >= 0.003 && abs(velocity.y - gravity / 16.0) < 0.003) {
                -0.003
            } else {
                velocity.y - gravity / 16.0
            }
        }
        return velocity
    }

    open fun createModel(): FluidModel? {
        return null
    }
}
