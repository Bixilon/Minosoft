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

package de.bixilon.minosoft.local.generator.flat

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.Dirt
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Bedrock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class FlatGenerator(
    val biome: Biome?,
    val layers: Array<BlockState?>,
) : ChunkGenerator {
    private val data = bake()
    private val biomeSource = DummyBiomeSource(biome)

    private fun bake(): Array<Array<BlockState?>?> {
        val sections = layers.size.sectionHeight + 1
        val array = arrayOfNulls<Array<BlockState?>?>(sections)

        for ((index, state) in this.layers.withIndex()) {
            var section = array[index.sectionHeight]
            if (section == null) {
                section = arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION)
                array[index.sectionHeight] = section
            }
            val y = index.inSectionHeight

            for (xz in 0 until ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z) {
                val position = InSectionPosition(xz).with(y = y)
                section[position.index] = state
            }
        }

        return array
    }

    override fun generate(chunk: Chunk) {
        chunk.biomeSource = this.biomeSource

        val minSection = chunk.world.dimension.minSection
        for ((index, data) in this.data.withIndex()) {
            if (data == null) continue
            val section = chunk.getOrPut(index + minSection) ?: continue
            val clone = data.clone()

            section.blocks.setData(clone)
        }
    }

    companion object {

        fun default(session: PlaySession): FlatGenerator {
            val bedrock = session.registries.block[Bedrock]?.states?.default
            val stone = session.registries.block[StoneBlock.Block]?.states?.default
            val dirt = session.registries.block[Dirt]?.states?.default
            val grass = session.registries.block[GrassBlock]?.states?.default

            val plains = session.registries.biome[minecraft("plains")] ?: Biome(minecraft("plains"), waterColor = RGBColor(0x3F76E4), temperature = 0.8f, downfall = 0.4f, fogColor = RGBColor(0xC0D8FF), skyColor = RGBColor(0x78A7FF))

            return FlatGenerator(plains, arrayOf(
                bedrock,
                stone,
                stone,
                stone,
                stone,
                stone,
                dirt,
                dirt,
                dirt,
                grass,
            ))
        }
    }
}
