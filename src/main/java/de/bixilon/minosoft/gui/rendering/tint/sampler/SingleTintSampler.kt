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
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider

object SingleTintSampler : TintSampler {

    override fun getFluidTint(chunk: Chunk, position: BlockPosition, provider: TintProvider): RGBColor {
        val biome = chunk.world.biomes.accessor[chunk, position.inChunkPosition]
        return provider.getFluidTint(biome, position)
    }

    override fun getBlockTint(chunk: Chunk, state: BlockState, position: BlockPosition, result: RGBArray, provider: TintProvider) {
        val biome = chunk.world.biomes.accessor[chunk, position.inChunkPosition]
        for (tintIndex in 0 until provider.count) {
            result[tintIndex] = provider.getBlockTint(state, biome, position, tintIndex)
        }
    }

    override fun sampleCustom(chunk: Chunk, position: BlockPosition, sampler: (Biome) -> RGBColor?): RGBColor? {
        val biome = chunk.world.biomes.accessor[chunk, position.inChunkPosition] ?: return null
        return sampler.invoke(biome)
    }
}
