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

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.fluid.Fluid
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer

class FluidBlock(
    resourceLocation: ResourceLocation,
    explosionResistance: Float = 0.0f,
    hasDynamicShape: Boolean = false,
    tintColor: RGBColor? = null,
    itemId: Int = 0,
    tint: ResourceLocation? = null,
    val stillFluid: Fluid,
    val flowingFluid: Fluid,
    renderOverride: MutableList<BlockLikeRenderer> = mutableListOf(),
) : Block(resourceLocation, explosionResistance, hasDynamicShape, tintColor, itemId, tint, renderOverride) {
    val fluidRenderer: FluidRenderer = FluidRenderer(this, stillFluid, flowingFluid)

    init {
        renderOverride.add(fluidRenderer)
    }
}
