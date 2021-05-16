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

package de.bixilon.minosoft.data.mappings.blocks

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.fluid.Fluid
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer

class FluidBlock : Block {
    override val renderOverride: List<BlockLikeRenderer>

    open val stillFluid: Fluid
    open val flowingFluid: Fluid

    val fluidRenderer: FluidRenderer


    constructor(resourceLocation: ResourceLocation, mappings: VersionMapping, data: JsonObject) : super(resourceLocation, mappings, data) {
        stillFluid = mappings.fluidRegistry.get(data["still_fluid"].asInt)
        flowingFluid = mappings.fluidRegistry.get(data["flow_fluid"].asInt)

        fluidRenderer = FluidRenderer(this, stillFluid, flowingFluid)
        renderOverride = listOf(fluidRenderer)
    }
}
