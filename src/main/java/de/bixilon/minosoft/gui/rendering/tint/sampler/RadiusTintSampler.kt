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

class RadiusTintSampler(radius: Int = 5) : MultiColorSampler(radius) {


    // TODO: check if biome source actually supports 3d biomes
    override fun getFluidTint(chunk: Chunk, position: BlockPosition, provider: TintProvider): RGBColor {
        sampled.clear()

        sampleFluid(chunk, position, BlockPosition(0, 0, 0), 100, provider)

        val diameter = radius * radius

        var offset = 1
        while (offset < radius) {
            val distance = offset * offset

            var weight = (diameter * 100) / (diameter + distance * 3)
            sampleFluid(chunk, position, BlockPosition(-offset, 0, 0), weight, provider)
            sampleFluid(chunk, position, BlockPosition(offset, 0, 0), weight, provider)
            sampleFluid(chunk, position, BlockPosition(0, 0, -offset), weight, provider)
            sampleFluid(chunk, position, BlockPosition(0, 0, offset), weight, provider)

            weight = (diameter * 100) / (diameter + distance * 6)
            sampleFluid(chunk, position, BlockPosition(-offset, 0, -offset), weight, provider)
            sampleFluid(chunk, position, BlockPosition(-offset, 0, offset), weight, provider)
            sampleFluid(chunk, position, BlockPosition(offset, 0, -offset), weight, provider)
            sampleFluid(chunk, position, BlockPosition(offset, 0, offset), weight, provider)

            offset += radius / 5
        }

        return sampled.toColor()
    }


    override fun getBlockTint(chunk: Chunk, state: BlockState, position: BlockPosition, result: RGBArray, provider: TintProvider) {
        ensureSize(provider.count)
        for (index in 0 until provider.count) {
            this.multiple[index].clear()
        }


        sampleBlock(chunk, state, position, BlockPosition(0, 0, 0), 100, provider)

        val diameter = radius * radius

        var offset = 1
        while (offset < radius) {
            val distance = offset * offset

            var weight = (diameter * 100) / (diameter + distance * 3)
            sampleBlock(chunk, state, position, BlockPosition(-offset, 0, 0), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(offset, 0, 0), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(0, 0, -offset), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(0, 0, offset), weight, provider)

            weight = (diameter * 100) / (diameter + distance * 6)
            sampleBlock(chunk, state, position, BlockPosition(-offset, 0, -offset), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(-offset, 0, offset), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(offset, 0, -offset), weight, provider)
            sampleBlock(chunk, state, position, BlockPosition(offset, 0, offset), weight, provider)

            offset += radius / 5
        }

        for (index in 0 until provider.count) {
            result[index] = this.multiple[index].toColor()
        }
    }

    override fun sampleCustom(chunk: Chunk, position: BlockPosition, sampler: (Biome) -> RGBColor?): RGBColor? {
        sampled.clear()

        sampleCustom(chunk, position, BlockPosition(0, 0, 0), 100, sampler)

        val diameter = radius * radius

        var offset = 1
        while (offset < radius) {
            val distance = offset * offset

            var weight = (diameter * 100) / (diameter + distance * 3)
            sampleCustom(chunk, position, BlockPosition(-offset, 0, 0), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(offset, 0, 0), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(0, 0, -offset), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(0, 0, offset), weight, sampler)

            weight = (diameter * 100) / (diameter + distance * 6)
            sampleCustom(chunk, position, BlockPosition(-offset, 0, -offset), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(-offset, 0, offset), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(offset, 0, -offset), weight, sampler)
            sampleCustom(chunk, position, BlockPosition(offset, 0, offset), weight, sampler)

            offset += radius / 5
        }

        return sampled.toNullColor()
    }
}
