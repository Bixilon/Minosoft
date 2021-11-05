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

package de.bixilon.minosoft.gui.rendering.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i
import java.util.*

class SectionPreparer(
    val renderWindow: RenderWindow,
) {


    fun prepare(section: ChunkSection): ChunkSectionMesh {
        val startTime = System.nanoTime()
        val mesh = ChunkSectionMesh(renderWindow)

        val random = Random(0L)
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    val block = section.blocks[ChunkSection.getIndex(x, y, z)] ?: continue

                    val neighbours: Array<BlockState?> = arrayOfNulls(Directions.VALUES.size)

                    // ToDo: Chunk borders
                    neighbours[Directions.DOWN.ordinal] = if (y == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y - 1, z)]
                    }
                    neighbours[Directions.UP.ordinal] = if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y + 1, z)]
                    }
                    neighbours[Directions.NORTH.ordinal] = if (z == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y, z - 1)]
                    }
                    neighbours[Directions.SOUTH.ordinal] = if (z == ProtocolDefinition.SECTION_MAX_Z) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x, y, z + 1)]
                    }
                    neighbours[Directions.WEST.ordinal] = if (x == 0) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x - 1, y, z)]
                    }
                    neighbours[Directions.EAST.ordinal] = if (x == ProtocolDefinition.SECTION_MAX_X) {
                        null
                    } else {
                        section.blocks[ChunkSection.getIndex(x + 1, y, z)]
                    }
                    val model = block.model

                    random.setSeed(0L)
                    model?.singleRender(Vec3i(x, y, z), mesh, random, neighbours, 0xFF, intArrayOf(0xF, 0xF, 0xF, 0xF))
                }
            }
        }


        val time = System.nanoTime()
        val delta = time - startTime
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Preparing took ${delta}ns, ${delta / 1000}µs, ${delta / 1000000}ms" }

        return mesh
    }
}
