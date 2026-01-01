/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider

abstract class MultiColorSampler(val radius: Int) : TintSampler {
    protected val sampled = SampledColor()
    protected var multiple = Array(1) { SampledColor() }

    init {
        assert(radius >= 0) { "Invalid radius: $radius" }
    }


    protected fun ensureSize(size: Int) {
        if (multiple.size >= size) return

        this.multiple = Array(size) { SampledColor() }
    }

    protected fun sampleFluid(chunk: Chunk, position: BlockPosition, offset: BlockPosition, weight: Int, provider: TintProvider) {
        val biome = chunk.neighbours.traceBiome(position.inChunkPosition, offset) ?: return

        val color = provider.getFluidTint(biome, position + offset)
        if (color == Colors.TRUE_BLACK) return

        sampled.add(color, weight)
    }

    protected fun sampleBlock(chunk: Chunk, state: BlockState, position: BlockPosition, offset: BlockPosition, weight: Int, provider: TintProvider) {
        val biome = chunk.neighbours.traceBiome(position.inChunkPosition, offset) ?: return

        for (index in 0 until provider.count) {
            val color = provider.getBlockTint(state, biome, position + offset, index)
            if (color == Colors.TRUE_BLACK) continue

            multiple[index].add(color, weight)
        }
    }

    protected fun sampleCustom(chunk: Chunk, position: BlockPosition, offset: BlockPosition, weight: Int, sampler: (Biome) -> RGBColor?) {
        val biome = chunk.neighbours.traceBiome(position.inChunkPosition, offset) ?: return

        val color = sampler.invoke(biome) ?: return

        sampled.add(color, weight)
    }
}
