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

package de.bixilon.minosoft.data.registries.fluid.fluids

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.minecraft

@Deprecated("null")
class EmptyFluid(resourceLocation: ResourceLocation = this.resourceLocation) : Fluid(resourceLocation) {

    override fun matches(other: Fluid): Boolean {
        return other is EmptyFluid
    }

    override fun matches(other: BlockState?): Boolean {
        other ?: return true
        if (other.block !is FluidBlock) {
            return true
        }
        return matches(other.block.fluid)
    }

    companion object : FluidFactory<EmptyFluid> {
        override val resourceLocation = minecraft("empty")

        override fun build(resourceLocation: ResourceLocation, registries: Registries) = EmptyFluid()
    }
}
