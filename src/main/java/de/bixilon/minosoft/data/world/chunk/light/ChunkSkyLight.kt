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
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ChunkSkyLight(val light: ChunkLight) {
    private val chunk = light.chunk


    fun calculate() {
        if (!chunk.world.dimension.hasSkyLight() || !chunk.neighbours.complete) {
            // no need to calculate it
            return
        }
        floodFill()
    }

    private fun traceSection(sectionHeight: SectionHeight, x: Int, topY: Int, bottomY: Int, z: Int, target: Directions) {
        val section = chunk.getOrPut(sectionHeight) ?: return
        val baseY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y

        for (y in topY downTo bottomY) {
            section.light.traceSkyLightIncrease(x, y, z, NEIGHBOUR_TRACE_LEVEL, target, baseY + y, false)
        }
        section.light.update = true
    }

    private fun trace(x: Int, topY: Int, bottomY: Int, z: Int, target: Directions) {
        if (topY == Int.MIN_VALUE) return // no blocks are set in that column, no need to trace. all levels are MAX
        if (bottomY > topY) return // started position is higher than at this, no need to trace

        // trace section after section
        val topSection = topY.sectionHeight
        val bottomSection = bottomY.sectionHeight

        val sections = topSection - maxOf(chunk.minSection, bottomSection) + 1


        // top section
        if (sections > 1) {
            traceSection(topSection, x, topY.inSectionHeight, 0, z, target)
        }

        // middle sections
        for (sectionHeight in (topSection - 1) downTo maxOf(chunk.minSection, bottomSection) + 1) {
            traceSection(sectionHeight, x, ProtocolDefinition.SECTION_MAX_Y, 0, z, target)
        }

        // lowest section
        traceSection(if (bottomY == Int.MIN_VALUE) chunk.minSection else bottomSection, x, if (topSection == bottomSection) topY.inSectionHeight else ProtocolDefinition.SECTION_MAX_Y, if (bottomY == Int.MIN_VALUE) 0 else bottomY.inSectionHeight, z, target)

        if (bottomY == Int.MIN_VALUE) {
            chunk.light.bottom.traceSkyIncrease(x, z, NEIGHBOUR_TRACE_LEVEL)
        }
    }

    private fun traceDown(x: Int, y: Int, z: Int) {
        val sectionHeight = y.sectionHeight
        if (sectionHeight == chunk.minSection - 1) {
            chunk.light.bottom.traceSkyIncrease(x, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I)
            return
        }
        val section = chunk[y.sectionHeight] ?: return
        section.light.traceSkyLightIncrease(x, y.inSectionHeight, z, ProtocolDefinition.MAX_LIGHT_LEVEL_I, null, y, true)
    }

    private fun floodFill(neighbours: Array<Chunk>, x: Int, z: Int) {
        val heightmapIndex = (z shl 4) or x
        val maxHeight = light.heightmap[heightmapIndex]


        traceDown(x, maxHeight, z)

        if (x > 0) {
            trace(x - 1, light.heightmap[heightmapIndex - 1], maxHeight, z, Directions.WEST)
        } else {
            val neighbour = neighbours[ChunkNeighbours.WEST].light
            neighbour.sky.trace(ProtocolDefinition.SECTION_MAX_X, neighbour.heightmap[(z shl 4) or ProtocolDefinition.SECTION_MAX_X], maxHeight, z, Directions.WEST)
        }

        if (x < ProtocolDefinition.SECTION_MAX_X) {
            trace(x + 1, light.heightmap[heightmapIndex + 1], maxHeight, z, Directions.EAST)
        } else {
            val neighbour = neighbours[ChunkNeighbours.EAST].light
            neighbour.sky.trace(0, neighbour.heightmap[(z shl 4) or 0], maxHeight, z, Directions.EAST)
        }

        if (z > 0) {
            trace(x, light.heightmap[((z - 1) shl 4) or x], maxHeight, z - 1, Directions.NORTH)
        } else {
            val neighbour = neighbours[ChunkNeighbours.NORTH].light
            neighbour.sky.trace(x, neighbour.heightmap[(ProtocolDefinition.SECTION_MAX_Z shl 4) or x], maxHeight, ProtocolDefinition.SECTION_MAX_Z, Directions.NORTH)
        }

        if (z < ProtocolDefinition.SECTION_MAX_Z) {
            trace(x, light.heightmap[((z + 1) shl 4) or x], maxHeight, z + 1, Directions.SOUTH)
        } else {
            val neighbour = neighbours[ChunkNeighbours.SOUTH].light
            neighbour.sky.trace(x, neighbour.heightmap[(0 shl 4) or x], maxHeight, 0, Directions.SOUTH)
        }
    }

    fun floodFill(x: Int, z: Int) {
        val neighbours = chunk.neighbours.get() ?: return
        floodFill(neighbours, x, z)
    }

    private fun floodFill() {
        val neighbours = this.chunk.neighbours.get() ?: return
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                floodFill(neighbours, x, z)
            }
        }
    }

    fun recalculate(sectionHeight: Int) {
        val minY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y

        // TODO: clear neighbours and let them propagate?
        // TODO: Optimize for specific section height (i.e. not trace everything above)
        calculate()
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


    private companion object {
        const val NEIGHBOUR_TRACE_LEVEL = ProtocolDefinition.MAX_LIGHT_LEVEL_I - 1
    }
}
