/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.education.world

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Bedrock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Cobblestone
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.education.MinosoftEducation
import de.bixilon.minosoft.local.generator.flat.FlatGenerator
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EducationGenerator(session: PlaySession) : FlatGenerator(plains, session.getLayers()) {
    private val max = MinosoftEducation.config.world.size

    override fun generate(chunk: Chunk) {
        if (chunk.chunkPosition.x < -max || chunk.chunkPosition.x >= max) return
        if (chunk.chunkPosition.y < -max || chunk.chunkPosition.y >= max) return

        super.generate(chunk)
    }


    companion object {
        val plains = Biome(minecraft("plains"), temperature = 0.5f, downfall = 0.5f, waterColor = RGBColor(0x3F76E4), skyColor = RGBColor(0x78A7FF))


        private fun PlaySession.getLayers(): Array<BlockState?> {
            val layers: MutableList<BlockState?> = mutableListOf()

            layers += registries.block[Bedrock]?.states?.default
            layers += registries.block[Cobblestone.Block]?.states?.default
            layers += registries.block[Cobblestone.Block]?.states?.default
            layers += registries.block[StoneBlock.Block]?.states?.default
            layers += registries.block[StoneBlock.Block]?.states?.default
            layers += registries.block[MinecraftBlocks.DIRT]?.states?.default
            layers += registries.block[MinecraftBlocks.DIRT]?.states?.default
            layers += registries.block[GrassBlock]?.states?.default

            return layers.toTypedArray()
        }
    }
}
