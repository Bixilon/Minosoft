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

import de.bixilon.kmath.number.IntUtil.pow
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider

class RadiusTintSampler(val radius: Int = 5) : TintSampler {
    private val sampled = SampledColor()

    private inline fun SampledColor.sample(chunk: Chunk, position: BlockPosition, offset: BlockPosition, weight: Int, sampler: (biome: Biome, position: BlockPosition) -> RGBColor) {
        val biome = chunk.neighbours.traceBiome(position.inChunkPosition, offset) ?: return

        val color = sampler.invoke(biome, position + offset)
        if (color == Colors.TRUE_BLACK) return

        this.add(color, weight)
    }

    // TODO: check if biome source actually supports 3d biomes
    override fun getFluidTint(chunk: Chunk, position: BlockPosition, provider: TintProvider): RGBColor {
        sampled.clear()

        sampled.sample(chunk, position, BlockPosition(0, 0, 0), 10, provider::getFluidTint)

        var offset = 1
        while (true) {
            val weight = (radius - offset).pow(2)
            sampled.sample(chunk, position, BlockPosition(-radius, 0, 0), weight, provider::getFluidTint)
            sampled.sample(chunk, position, BlockPosition(radius, 0, 0), weight, provider::getFluidTint)

            sampled.sample(chunk, position, BlockPosition(0, -radius, 0), weight, provider::getFluidTint)
            sampled.sample(chunk, position, BlockPosition(0, radius, 0), weight, provider::getFluidTint)

            sampled.sample(chunk, position, BlockPosition(0, 0, -radius), weight, provider::getFluidTint)
            sampled.sample(chunk, position, BlockPosition(0, 0, radius), weight, provider::getFluidTint)

            offset += radius / 3
            if (offset >= radius) break
        }

        return sampled.toColor()
    }
}
