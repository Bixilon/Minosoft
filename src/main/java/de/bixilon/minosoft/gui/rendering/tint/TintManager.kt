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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class TintManager(private val connection: PlayConnection) {
    private val grassTintCalculator = GrassTintCalculator()
    private val foliageTintCalculator = FoliageTintCalculator()

    fun init(assetsManager: AssetsManager) {
        grassTintCalculator.init(assetsManager)
        foliageTintCalculator.init(assetsManager)

        val blockRegistry = connection.registries.blockRegistry
        for ((blockNames, provider) in createDefaultTints()) {
            for (blockName in blockNames) {
                val block = blockRegistry[blockName] ?: continue
                block.tintProvider = provider
            }
        }
    }

    private fun getAverageBlockTint(chunk: Chunk, neighbours: Array<Chunk>, blockState: BlockState, tintProvider: TintProvider, x: Int, y: Int, z: Int): IntArray {
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
        return getAverageBlockTint(chunk, neighbours, blockState, fluid.tintProvider ?: return null, x, y, z)
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


    private fun createDefaultTints(): Map<Set<ResourceLocation>, TintProvider> {
        return mapOf(
            setOf(MinecraftBlocks.GRASS_BLOCK, MinecraftBlocks.FERN, MinecraftBlocks.GRASS, MinecraftBlocks.POTTED_FERN) to grassTintCalculator,
            setOf(MinecraftBlocks.LARGE_FERN, MinecraftBlocks.TALL_GRASS) to TallGrassTintCalculator(grassTintCalculator),
            setOf(MinecraftBlocks.SPRUCE_LEAVES) to StaticTintProvider(0x619961),
            setOf(MinecraftBlocks.BIRCH_LEAVES) to StaticTintProvider(0x80A755),
            setOf(MinecraftBlocks.OAK_LEAVES, MinecraftBlocks.JUNGLE_LEAVES, MinecraftBlocks.ACACIA_LEAVES, MinecraftBlocks.DARK_OAK_LEAVES, MinecraftBlocks.VINE) to foliageTintCalculator, setOf(MinecraftBlocks.REDSTONE_WIRE) to RedstoneWireTintCalculator,
            setOf(MinecraftBlocks.WATER_CAULDRON, MinecraftBlocks.CAULDRON, MinecraftBlocks.WATER) to WaterTintProvider,
            setOf(MinecraftBlocks.SUGAR_CANE) to SugarCaneTintCalculator(grassTintCalculator),
            setOf(MinecraftBlocks.ATTACHED_MELON_STEM, MinecraftBlocks.ATTACHED_PUMPKIN_STEM) to StaticTintProvider(0xE0C71C),
            setOf(MinecraftBlocks.MELON_STEM, MinecraftBlocks.PUMPKIN_STEM) to StemTintCalculator,
            setOf(MinecraftBlocks.LILY_PAD) to StaticTintProvider(block = 0x208030, item = 0x71C35C),
        )
    }

    companion object {
        const val DEFAULT_TINT_INDEX = -1

        fun getJsonColor(color: Int): RGBColor? {
            if (color == 0) {
                return null
            }
            return color.asRGBColor()
        }
    }
}
