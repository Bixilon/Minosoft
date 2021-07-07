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
package de.bixilon.minosoft.data.registries.fluid

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.math.abs
import kotlin.random.Random

open class Fluid(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem() {
    open val stillTexture: ResourceLocation? = null
    val dripParticle: ParticleType? = null
    val bucketItem: Item? = null

    init {
        this::bucketItem.inject(data["bucket"])
        this::dripParticle.inject(data["drip_particle_type"])
    }

    override fun toString(): String {
        return resourceLocation.full
    }

    open fun matches(other: Fluid): Boolean {
        return other == this
    }

    open fun matches(other: BlockState?): Boolean {
        other ?: return false
        if (other.block !is FluidBlock) {
            return false
        }

        return matches(other.block.fluid)
    }

    fun getHeight(blockState: BlockState): Float {
        return (8 - ((blockState.properties[BlockProperties.FLUID_LEVEL]?.unsafeCast<Int>()) ?: 8)) / 9.0f
    }

    open fun travel(entity: LocalPlayerEntity, sidewaysSpeed: Float, forwardSpeed: Float, gravity: Double, falling: Boolean) {
        entity.accelerate(sidewaysSpeed, forwardSpeed, 0.02)
    }

    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {}


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

    companion object : ResourceLocationDeserializer<Fluid> {

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Fluid {
            check(registries != null) { "Registries is null!" }
            DefaultFluidFactories[data["class"]?.nullCast<String>()]?.let {
                return it.build(resourceLocation, registries, data)
            }

            return Fluid(resourceLocation, registries, data)
        }
    }
}
