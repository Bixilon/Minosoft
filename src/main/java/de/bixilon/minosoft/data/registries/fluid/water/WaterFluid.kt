/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.fluid.water

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.enchantment.DefaultEnchantments
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.water.UnderwaterParticle
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.WaterTintProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3i
import kotlin.random.Random

class WaterFluid(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : FlowableFluid(resourceLocation, registries, data) {
    private val depthStriderEnchantment: Enchantment = unsafeNull()
    private val dolphinsGraceStatusEffect: StatusEffect = unsafeNull()
    override val stillTextureName: ResourceLocation = "minecraft:block/water_still".toResourceLocation()
    override val flowingTextureName: ResourceLocation = "minecraft:block/water_flow".toResourceLocation()
    override val tintProvider: TintProvider = WaterTintProvider


    init {
        this::depthStriderEnchantment.inject(DefaultEnchantments.DEPTH_STRIDER)
        this::dolphinsGraceStatusEffect.inject(DefaultStatusEffects.DOLPHINS_GRACE)
    }

    override fun matches(other: Fluid): Boolean {
        return other is WaterFluid
    }

    override fun matches(other: BlockState?): Boolean {
        other ?: return false
        if (other.properties[BlockProperties.WATERLOGGED] == true) {
            return true
        }
        return super.matches(other)
    }


    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)

        // ToDo: if not sill and not falling
        if (random.chance(10)) {
            connection.world += UnderwaterParticle(connection, blockPosition.toVec3d + { random.nextDouble() })
        }
    }

    companion object : FluidFactory<WaterFluid>, MultiClassFactory<WaterFluid> {
        private const val VELOCITY_MULTIPLIER = 0.014
        override val ALIASES: Set<String> = setOf("WaterFluid\$Flowing", "WaterFluid\$Still")

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): WaterFluid {
            return WaterFluid(resourceLocation, registries, data)
        }
    }
}
