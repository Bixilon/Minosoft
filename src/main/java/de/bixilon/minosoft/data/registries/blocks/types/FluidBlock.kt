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

package de.bixilon.minosoft.data.registries.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast
import glm_.vec3.Vec3i
import kotlin.random.Random

open class FluidBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {
    open val stillFluid: Fluid = registries.fluidRegistry[data["still_fluid"].asInt]
    open val flowingFluid: Fluid = registries.fluidRegistry[data["flow_fluid"].asInt]

    val fluidRenderer: FluidRenderer

    override val renderOverride: List<BlockLikeRenderer>

    init {
        let {
            fluidRenderer = FluidRenderer(it, stillFluid, flowingFluid)
            renderOverride = listOf(fluidRenderer)
        }
    }

    fun getFluidHeight(blockState: BlockState): Float {
        return (blockState.properties[BlockProperties.FLUID_LEVEL]?.nullCast() ?: 0) * FLUID_HEIGHT_CALCULATOR
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)
        // ToDO
        stillFluid.randomTick(connection, blockState, blockPosition, random)
    }

    companion object {
        private const val FLUID_HEIGHT_CALCULATOR = 1.0f / 16.0f
    }
}
