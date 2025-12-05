/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.tint.sampler

import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider

interface TintSampler {

    fun getFluidTint(chunk: Chunk, fluid: Fluid, height: Float, position: BlockPosition, provider: TintProvider): RGBColor


    companion object {

        // TODO: Optimize the special case of VoronoiBiomeAccessor (we know what points are next and then can use sample those instead of the naive gaussian sampler)
        fun of(enabled: Boolean, radius: Int) = when {
            !enabled || radius <= 0 -> SingleTintSampler()
            else -> GaussianTintSampler(radius)
        }
    }
}
