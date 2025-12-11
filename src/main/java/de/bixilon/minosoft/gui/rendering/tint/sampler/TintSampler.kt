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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintProviderFlags
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock

interface TintSampler {

    fun getFluidTint(chunk: Chunk, position: BlockPosition, provider: TintProvider): RGBColor

    fun getFluidTint(chunk: Chunk, fluid: Fluid, position: BlockPosition): RGBColor {
        val provider = fluid.model?.tint ?: return Colors.WHITE_RGB
        if (TintProviderFlags.BIOME in provider.flags) {
            return getFluidTint(chunk, position, provider)
        }
        return SingleTintSampler.getFluidTint(chunk, position, provider)
    }

    fun getBlockTint(chunk: Chunk, state: BlockState, position: BlockPosition, result: RGBArray, provider: TintProvider)


    fun getBlockTint(chunk: Chunk, state: BlockState, position: BlockPosition, cache: RGBArray?): RGBArray? {
        if (BlockStateFlags.TINTED !in state.flags) return null
        val provider = state.block.unsafeCast<TintedBlock>().tintProvider ?: return null

        val size = provider.count
        val tints = if (cache != null && cache.size >= size) cache else RGBArray(size)

        if (TintProviderFlags.BIOME in provider.flags) {
            getBlockTint(chunk, state, position, tints, provider)
        } else {
            SingleTintSampler.getBlockTint(chunk, state, position, tints, provider)
        }

        return tints
    }


    companion object {

        // TODO: Optimize the special case of VoronoiBiomeAccessor (we know what points are next and then can use sample those instead of the naive gaussian sampler)
        fun of(enabled: Boolean, radius: Int) = when {
            !enabled || radius <= 0 -> SingleTintSampler
            else -> RadiusTintSampler(radius)
        }
    }
}
