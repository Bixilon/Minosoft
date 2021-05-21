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

package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.fluid.Fluid
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer

open class FluidBlock(resourceLocation: ResourceLocation, mappings: Registries, data: JsonObject) : Block(resourceLocation, mappings, data) {
    open val stillFluid: Fluid = mappings.fluidRegistry[data["still_fluid"].asInt]
    open val flowingFluid: Fluid = mappings.fluidRegistry[data["flow_fluid"].asInt]

    val fluidRenderer: FluidRenderer

    override val renderOverride: List<BlockLikeRenderer>

    init {
        also {
            fluidRenderer = FluidRenderer(it, stillFluid, flowingFluid)
            renderOverride = listOf(fluidRenderer)
        }
    }

}
