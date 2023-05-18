/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class TintManager(val connection: PlayConnection) {
    val grassTintCalculator = GrassTintCalculator()
    val foliageTintCalculator = FoliageTintCalculator()

    fun init(assetsManager: AssetsManager) {
        grassTintCalculator.init(assetsManager)
        foliageTintCalculator.init(assetsManager)

        DefaultTints.init(this)
    }

    private fun getAverageBlockTint(chunk: Chunk, neighbours: Array<Chunk>, blockState: BlockState, tintProvider: TintProvider, x: Int, y: Int, z: Int): IntArray {
        // ToDo: biome blending?
        val inChunkX = x and 0x0F
        val inChunkZ = z and 0x0F
        val biome = chunk.getBiome(inChunkX, y, inChunkZ)
        val tints = IntArray(if (tintProvider is MultiTintProvider) tintProvider.tints else 1)

        for (tintIndex in tints.indices) {
            tints[tintIndex] = tintProvider.getBlockColor(blockState, biome, x, y, z, tintIndex)
        }
        return tints
    }

    fun getAverageBlockTint(chunk: Chunk, neighbours: Array<Chunk>, blockState: BlockState, x: Int, y: Int, z: Int): IntArray? {
        return getAverageBlockTint(chunk, neighbours, blockState, blockState.block.tintProvider ?: return null, x, y, z)
    }

    fun getAverageBlockTint(chunk: Chunk, neighbours: Array<Chunk>, blockState: BlockState, fluid: Fluid, x: Int, y: Int, z: Int): IntArray? {
        return getAverageBlockTint(chunk, neighbours, blockState, fluid.model?.tint ?: return null, x, y, z)
    }

    fun getBlockTint(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): IntArray? {
        val tintProvider = blockState.block.tintProvider ?: return null
        val tints = IntArray(if (tintProvider is MultiTintProvider) tintProvider.tints else 1)

        for (tintIndex in tints.indices) {
            tints[tintIndex] = tintProvider.getBlockColor(blockState, biome, x, y, z, tintIndex)
        }
        return tints
    }

    fun getParticleTint(blockState: BlockState, x: Int, y: Int, z: Int): Int? {
        val tintProvider = blockState.block.tintProvider ?: return null
        val biome = connection.world.getBiome(x, y, z)
        return tintProvider.getParticleColor(blockState, biome, x, y, z)
    }

    fun getParticleTint(blockState: BlockState, position: Vec3i): Int? {
        return getParticleTint(blockState, position.x, position.y, position.z)
    }

    fun getBlockTint(blockState: BlockState, biome: Biome? = null, blockPosition: Vec3i): IntArray? {
        return getBlockTint(blockState, biome, blockPosition.x, blockPosition.y, blockPosition.z)
    }

    fun getFluidTint(chunk: Chunk, fluid: Fluid, height: Float, x: Int, y: Int, z: Int): Int? {
        val biome = chunk.getBiome(x and 0x0F, y, z and 0x0F)
        return fluid.model?.tint?.getFluidTint(fluid, biome, height, x, y, z)
    }

    fun getItemTint(stack: ItemStack): IntArray? {
        val tintProvider = stack.item.item.tintProvider ?: return null
        val tints = IntArray(if (tintProvider is MultiTintProvider) tintProvider.tints else 1)

        for (tintIndex in tints.indices) {
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
            return color.asRGBColor()
        }

        fun Any?.jsonTint(): RGBColor? {
            val rgb = this?.toInt() ?: return null
            return getJsonColor(rgb)
        }
    }
}
