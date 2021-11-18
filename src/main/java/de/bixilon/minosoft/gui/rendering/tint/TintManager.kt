/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3i

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

    fun getAverageTint(chunk: Chunk, neighbours: Array<Chunk>, blockState: BlockState, x: Int, y: Int, z: Int): IntArray? {
        val tintProvider = blockState.block.tintProvider ?: return null
        val inChunkX = x and 0x0F
        val inChunkZ = z and 0x0F
        val biome = chunk.getBiome(inChunkX, y, inChunkZ)
        val tints = IntArray(if (tintProvider is MultiTintProvider) tintProvider.tints else 1)

        for (tintIndex in tints.indices) {
            tints[tintIndex] = tintProvider.getColor(blockState, biome, x, y, z, tintIndex)
        }
        return tints
    }

    fun getTint(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): IntArray? {
        val tintProvider = blockState.block.tintProvider ?: return null
        connection.world.getBiome(x, y, z)
        val tints = IntArray(if (tintProvider is MultiTintProvider) tintProvider.tints else 1)

        for (tintIndex in tints.indices) {
            tints[tintIndex] = tintProvider.getColor(blockState, biome, x, y, z, tintIndex)
        }
        return tints
    }

    fun getTint(blockState: BlockState, biome: Biome? = null, blockPosition: Vec3i): IntArray? {
        return getTint(blockState, biome, blockPosition.x, blockPosition.y, blockPosition.z)
    }


    private fun createDefaultTints(): Map<Set<ResourceLocation>, TintProvider> {
        return mapOf(
            setOf(MinecraftBlocks.GRASS_BLOCK, MinecraftBlocks.FERN, MinecraftBlocks.GRASS, MinecraftBlocks.POTTED_FERN) to grassTintCalculator,
            setOf(MinecraftBlocks.LARGE_FERN, MinecraftBlocks.TALL_GRASS) to TintProvider { blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int ->
                return@TintProvider if (blockState?.properties?.get(BlockProperties.STAIR_HALF) == Halves.UPPER) {
                    grassTintCalculator.getColor(blockState, biome, x, y - 1, z, tintIndex)
                } else {
                    grassTintCalculator.getColor(blockState, biome, x, y, z, tintIndex)
                }
            },
            setOf(MinecraftBlocks.SPRUCE_LEAVES) to StaticTintProvider(0x619961),
            setOf(MinecraftBlocks.BIRCH_LEAVES) to StaticTintProvider(0x80A755),
            setOf(MinecraftBlocks.OAK_LEAVES, MinecraftBlocks.JUNGLE_LEAVES, MinecraftBlocks.ACACIA_LEAVES, MinecraftBlocks.DARK_OAK_LEAVES, MinecraftBlocks.VINE) to foliageTintCalculator,
            // ToDo: Water
            setOf(MinecraftBlocks.REDSTONE_WIRE) to RedstoneWireTintCalculator,
            setOf(MinecraftBlocks.SUGAR_CANE) to TintProvider { blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int ->
                if (blockState == null || biome == null) {
                    return@TintProvider -1
                }
                return@TintProvider grassTintCalculator.getColor(blockState, biome, x, y, z, tintIndex)
            },
            setOf(MinecraftBlocks.ATTACHED_MELON_STEM, MinecraftBlocks.ATTACHED_PUMPKIN_STEM) to StaticTintProvider(0xE0C71C),
            setOf(MinecraftBlocks.MELON_STEM, MinecraftBlocks.PUMPKIN_STEM) to StemTintCalculator,
            setOf(MinecraftBlocks.LILY_PAD) to TintProvider { blockState: BlockState?, biome: Biome?, _: Int, _: Int, _: Int, _: Int -> if (blockState == null || biome == null) 0x71C35C else 0x208030 },
        )
    }

    companion object {

        fun getJsonColor(color: Int): RGBColor? {
            if (color == 0) {
                return null
            }
            return color.asRGBColor()
        }
    }
}
