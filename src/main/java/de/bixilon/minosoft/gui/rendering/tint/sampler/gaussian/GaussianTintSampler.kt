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

package de.bixilon.minosoft.gui.rendering.tint.sampler.gaussian

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.biome.source.BiomeSourceFlags
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.sampler.RadiusColorSampler

class GaussianTintSampler(radius: Int = 5) : RadiusColorSampler(radius) {
    private var kernel: GaussianSampleList? = null

    private fun getKernel(chunk: Chunk): GaussianSampleList? {
        kernel?.let { return it }
        val source = chunk.biomeSource ?: return null

        val kernel = when {
            BiomeSourceFlags.HORIZONTAL !in source.flags -> GaussianKernel.SINGLE
            BiomeSourceFlags.VERTICAL in source.flags -> GaussianKernel.get3D(radius)
            else -> GaussianKernel.get2D(radius)
        }
        this.kernel = kernel

        return kernel
    }

    override fun sampleFluid(chunk: Chunk, position: BlockPosition, provider: TintProvider) {
        getKernel(chunk)?.iterate { x, y, z, weight -> sampleFluid(chunk, position, BlockPosition(x, y, z), weight, provider) }
    }

    override fun sampleBlock(chunk: Chunk, state: BlockState, position: BlockPosition, provider: TintProvider) {
        getKernel(chunk)?.iterate { x, y, z, weight -> sampleBlock(chunk, state, position, BlockPosition(x, y, z), weight, provider) }
    }

    override fun sampleCustomColor(chunk: Chunk, position: BlockPosition, sampler: (Biome) -> RGBColor?) {
        getKernel(chunk)?.iterate { x, y, z, weight -> sampleCustom(chunk, position, BlockPosition(x, y, z), weight, sampler) }
    }
}
