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
import de.bixilon.minosoft.data.registries.blocks.DefaultBlocks
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3i

class TintManager(private val connection: PlayConnection) {
    private val grassTintCalculator = GrassTintCalculator()
    private lateinit var foliageColorMap: IntArray

    fun init(assetsManager: AssetsManager) {
        grassTintCalculator.init(assetsManager)
        foliageColorMap = assetsManager.readAGBArrayAsset("minecraft:colormap/foliage".toResourceLocation().texture())

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
        val tints = IntArray(tintProvider.indices)

        for (tintIndex in tints.indices) {
            tints[tintIndex] = tintProvider.getColor(blockState, biome, x, y, z, tintIndex)
        }
        return tints
    }

    fun getTint(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): Int? {
        TODO()
    }

    fun getTint(blockState: BlockState, biome: Biome? = null, blockPosition: Vec3i): Int? {
        return getTint(blockState, biome, blockPosition.x, blockPosition.y, blockPosition.z)
    }


    private fun createDefaultTints(): Map<Set<ResourceLocation>, TintProvider> {
        val defaultTints: Map<Set<ResourceLocation>, TintProvider> = mapOf(
            setOf(DefaultBlocks.GRASS_BLOCK) to grassTintCalculator
        )
        return defaultTints
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
