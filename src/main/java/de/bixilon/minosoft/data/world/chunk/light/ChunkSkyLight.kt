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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.ChunkLightUtil.hasSkyLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ChunkSkyLight(val light: ChunkLight) {
    private val chunk = light.chunk

    fun calculate() {
        if (!chunk.world.dimension.hasSkyLight() || !chunk.neighbours.complete) {
            // no need to calculate it
            return
        }
        val neighbours = this.chunk.neighbours.get() ?: return
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                floodFill(neighbours, x, z)
            }
        }
    }

    @Deprecated("unused")
    private fun getNeighbourMaxHeight(neighbours: Array<Chunk>, x: Int, z: Int, heightmapIndex: Int = (z shl 4) or x): IntArray {
        return intArrayOf(
            if (x > 0) {
                light.heightmap[heightmapIndex - 1]
            } else {
                neighbours[ChunkNeighbours.WEST].light.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X]
            },

            if (x < ProtocolDefinition.SECTION_MAX_X) {
                light.heightmap[heightmapIndex + 1]
            } else {
                neighbours[ChunkNeighbours.EAST].light.heightmap[(z shl 4) or 0]
            },

            if (z > 0) {
                light.heightmap[((z - 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.NORTH].light.heightmap[(ProtocolDefinition.SECTION_MAX_Z shl 4) or x]
            },

            if (z < ProtocolDefinition.SECTION_MAX_Z) {
                light.heightmap[((z + 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.SOUTH].light.heightmap[(0 shl 4) or x]
            }
        )
    }

    fun getNeighbourMinHeight(neighbours: Array<Chunk>, x: Int, z: Int, heightmapIndex: Int = (z shl 4) or x): Int {
        return minOf(
            if (x > 0) {
                light.heightmap[heightmapIndex - 1]
            } else {
                neighbours[ChunkNeighbours.WEST].light.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X]
            },

            if (x < ProtocolDefinition.SECTION_MAX_X) {
                light.heightmap[heightmapIndex + 1]
            } else {
                neighbours[ChunkNeighbours.EAST].light.heightmap[(z shl 4) or 0]
            },

            if (z > 0) {
                light.heightmap[((z - 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.NORTH].light.heightmap[(ProtocolDefinition.SECTION_MAX_Z shl 4) or x]
            },

            if (z < ProtocolDefinition.SECTION_MAX_Z) {
                light.heightmap[((z + 1) shl 4) or x]
            } else {
                neighbours[ChunkNeighbours.SOUTH].light.heightmap[(0 shl 4) or x]
            }
        )
    }

    private fun doWest(x: Int, topY: Int, bottomY: Int, z: Int) {
        if (topY == Int.MIN_VALUE) return // no blocks are set in that column, no need to trace. all levels are MAX
        if (bottomY > topY) return // started position is higher than at this, no need to trace


        // trace section after section
        val sectionStart = topY.sectionHeight
        val sectionEnd = bottomY.sectionHeight

        for (sectionHeight in sectionStart downTo (sectionEnd + 1)) {
            val section = chunk[sectionHeight] ?: continue
            section.light.traceSkyLightIncrease(x, 0, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I, Directions.EAST, 0, false)
        }

        // trace lowest section just from heightmap start
    }

    private fun floodFill(neighbours: Array<Chunk>, x: Int, z: Int) {
        val heightmapIndex = (z shl 4) or x
        val maxHeight = light.heightmap[heightmapIndex]


        if (x > 0) {
            doWest(x - 1, light.heightmap[heightmapIndex - 1], maxHeight, z)
        } else {
            val neighbour = neighbours[ChunkNeighbours.WEST].light
            neighbour.sky.doWest(ProtocolDefinition.SECTION_MAX_X, neighbour.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X], maxHeight, z)
        }


    }

    fun startFloodFill(x: Int, z: Int) {

    }

    fun recalculate(sectionHeight: Int) {
        val minY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y

        // TODO: clear neighbours and let them propagate?
        // TODO: Optimize for specific section height (i.e. not trace everything above)
        calculate()
    }
}
