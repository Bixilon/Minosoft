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

package de.bixilon.minosoft.data.world.chunk.light.section

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.heightmap.FixedHeightmap
import de.bixilon.minosoft.data.world.chunk.heightmap.LightHeightmap
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLightUtil.hasSkyLight
import de.bixilon.minosoft.data.world.chunk.light.section.border.BottomSectionLight
import de.bixilon.minosoft.data.world.chunk.light.section.border.TopSectionLight
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.*

class ChunkLight(val chunk: Chunk) {
    val heightmap = if (chunk.world.dimension.hasSkyLight()) LightHeightmap(chunk) else FixedHeightmap.MAX_VALUE

    val bottom = BottomSectionLight(chunk)
    val top = TopSectionLight(chunk)


    fun onBlockChange(position: InChunkPosition, section: ChunkSection, previous: BlockState?, next: BlockState?) {
        heightmap.onBlockChange(position, next)

        section.light.onBlockChange(position.inSectionPosition, previous, next)
        // TODO: trace skylight difference
    }


    operator fun get(position: InChunkPosition): LightLevel {
        val sectionHeight = position.sectionHeight
        val inSection = position.inSectionPosition

        val light = when (sectionHeight) {
            chunk.minSection - 1 -> bottom[inSection]
            chunk.maxSection + 1 -> top[inSection]
            else -> chunk[sectionHeight]?.light?.get(inSection) ?: LightLevel.EMPTY
        }

        if (position.y >= heightmap[position]) {
            return light.with(sky = LightLevel.MAX_LEVEL)
        }
        return light
    }

    fun clear() {
        bottom.clear()
        for (section in chunk.sections) {
            section?.light?.clear()
        }
        top.clear()
    }

    fun calculate() {
        for (section in chunk.sections) {
            section?.light?.calculateBlocks()
        }
        calculateSky()
    }

    fun propagate() {
        bottom.propagate()
        for (section in chunk.sections) {
            section?.light?.propagate()
        }
        top.propagate()
    }

    fun fireEvents() {
        bottom.fireEvent()?.fire(chunk.session)
        for (section in chunk.sections) {
            section?.light?.fireEvent()?.fire(chunk.session)
        }
        top.fireEvent()?.fire(chunk.session)
    }

    fun fireNeighbourEvents() {
        fireEvents()
        for (neighbour in chunk.neighbours.neighbours.array) {
            neighbour?.light?.fireEvents()
        }
    }

    private fun traceSkyDown(xz: InSectionPosition, topY: Int, bottomY: Int) {
        if (topY >= (chunk.maxSection + 1) * SECTION_HEIGHT_Y) return // no blocks are set in that column, no need to tracee
        val topSection = minOf(chunk.maxSection, topY.sectionHeight)
        val bottomSection = maxOf(chunk.minSection, bottomY.sectionHeight)

        if (bottomSection > topSection) return

        if (topSection > bottomSection) { // highest
            chunk[topSection]?.light?.traceSkyDown(xz, topY.inSectionHeight, 0)
        }

        for (height in (bottomSection + 1)..(topSection - 1)) { // all inbetween
            chunk[height]?.light?.traceSkyDown(xz, SECTION_MAX_Y, 0)
        }
        // lowest
        chunk[bottomSection]?.light?.traceSkyDown(xz, if (topSection == bottomSection) topY.inSectionHeight else SECTION_MAX_Y, bottomY.inSectionHeight)

        if (bottomY <= (chunk.minSection * SECTION_HEIGHT_Y)) {
            chunk.light.bottom.traceSky(xz)
        }
    }

    private fun maxOf(a: Int, b: Int, c: Int, d: Int): Int { // kotlins maxOf uses an vararg array (bad)
        return maxOf(a, maxOf(b, maxOf(c, d)))
    }

    private fun getNeighbourMinHeight(xz: InSectionPosition): Int {
        val heightmap = chunk.light.heightmap
        val neighbours = chunk.neighbours

        val west = if (xz.x == 0) neighbours[Directions.WEST]?.light?.heightmap?.get(xz.with(x = SECTION_MAX_X)) ?: Int.MIN_VALUE else heightmap[xz.minusX()]
        val east = if (xz.x == SECTION_MAX_X) neighbours[Directions.EAST]?.light?.heightmap?.get(xz.with(x = 0)) ?: Int.MIN_VALUE else heightmap[xz.plusX()]

        val north = if (xz.z == 0) neighbours[Directions.NORTH]?.light?.heightmap?.get(xz.with(z = SECTION_MAX_Z)) ?: Int.MIN_VALUE else heightmap[xz.minusZ()]
        val south = if (xz.z == SECTION_MAX_X) neighbours[Directions.SOUTH]?.light?.heightmap?.get(xz.with(z = 0)) ?: Int.MIN_VALUE else heightmap[xz.plusZ()]

        // TODO: Only trace in direction where heightmap is higher (separate for each horizontal direction)

        return maxOf(west, east, north, south)
    }

    fun calculateSky() {
        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            val position = InSectionPosition(xz)
            val bottomY = chunk.light.heightmap[xz]
            val topY = getNeighbourMinHeight(position)
            traceSkyDown(position, topY, maxOf(bottomY, chunk.minSection - 1))
        }
    }
}
