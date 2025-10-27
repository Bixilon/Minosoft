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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.pixlyzer.PixLyzerItem
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.tint.tints.grass.GrassTintCalculator
import de.bixilon.minosoft.gui.rendering.tint.tints.plants.FoliageTintCalculator
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class TintManager(val session: PlaySession) {
    val grass = GrassTintCalculator()
    val foliage = FoliageTintCalculator()

    fun init(assetsManager: AssetsManager) {
        grass.init(assetsManager)
        foliage.init(assetsManager)

        for (block in session.registries.block) {
            if (block !is TintedBlock) continue
            block.initTint(this)
        }
        for (item in session.registries.item) {
            if (item !is TintedBlock) continue
            item.initTint(this)
        }

        DefaultTints.init(this)
    }

    fun getBlockTint(state: BlockState, position: BlockPosition, biome: Biome?, cache: RGBArray?, tintProvider: TintProvider? = null): RGBArray? {
        if (BlockStateFlags.TINTED !in state.flags) return null
        val tintProvider = tintProvider ?: state.block.unsafeCast<TintedBlock>().tintProvider ?: return null

        val size = tintProvider.count
        val tints = if (cache != null && cache.size >= size) cache else RGBArray(size)

        for (tintIndex in 0 until size) {
            tints[tintIndex] = tintProvider.getBlockColor(state, biome, position, tintIndex)
        }

        return tints
    }

    fun getBlockTint(state: BlockState, chunk: Chunk, position: InChunkPosition, cache: RGBArray?): RGBArray? {
        if (BlockStateFlags.TINTED !in state.flags) return null
        val tintProvider = state.block.unsafeCast<TintedBlock>().tintProvider ?: return null

        val biome = chunk.getBiome(position)
        val offset = chunk.position.blockPosition(position)

        return getBlockTint(state, offset, biome, cache, tintProvider)
    }

    fun getParticleTint(state: BlockState, position: BlockPosition): RGBColor {
        if (state.block !is TintedBlock) return Colors.WHITE_RGB
        val tintProvider = state.block.tintProvider ?: return Colors.WHITE_RGB

        // TODO: cache chunk of particle
        val biome = session.world.biomes[position]
        return tintProvider.getParticleColor(state, biome, position)
    }

    fun getFluidTint(chunk: Chunk, fluid: Fluid, height: Float, position: BlockPosition): RGBColor {
        val provider = fluid.model?.tint ?: return Colors.WHITE_RGB
        val biome = chunk.getBiome(position.inChunkPosition)
        return provider.getFluidTint(fluid, biome, height, position)
    }

    private fun Item.getTintProvider(): TintProvider? {
        if (this is TintedBlock && tintProvider != null) return tintProvider
        if (this::class.java == Item::class.java && this !is PixLyzerItem) return null
        // TODO: dirty hack: get block
        val block = session.registries.block[identifier] ?: return null
        if (block !is TintedBlock) return null
        return block.tintProvider
    }

    fun getItemTint(stack: ItemStack): RGBArray? {
        val tintProvider = stack.item.getTintProvider() ?: return null
        val tints = RGBArray(tintProvider.count)

        for (tintIndex in tints.array.indices) {
            tints[tintIndex] = tintProvider.getItemColor(stack, tintIndex)
        }

        return tints
    }

    companion object {
        const val DEFAULT_TINT_INDEX = -1

        fun getJsonColor(color: Int): RGBColor? {
            if (color == 0) {
                return null
            }
            return color.rgb()
        }

        fun Any?.jsonTint(): RGBColor? {
            val rgb = this?.toInt() ?: return null
            return getJsonColor(rgb)
        }
    }
}
