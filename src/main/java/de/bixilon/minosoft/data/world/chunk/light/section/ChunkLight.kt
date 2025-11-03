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
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition

class ChunkLight(
    val chunk: Chunk,
) {
    val minSection = chunk.world.dimension.minSection
    val maxSection = chunk.world.dimension.maxSection
    val heightmap = if (chunk.world.dimension.hasSkyLight()) LightHeightmap(chunk) else FixedHeightmap.MAX_VALUE


    val bottom = BottomSectionLight(chunk)
    val top = TopSectionLight(chunk)

    val sky = ChunkSkyLight(this)


    fun onBlockChange(position: InChunkPosition, section: ChunkSection, previous: BlockState?, next: BlockState?) {
        heightmap.onBlockChange(position, next)

        section.light.onBlockChange(position.inSectionPosition, previous, next)

        if (!chunk.neighbours.complete) return

        fireLightChange(section, ChunkLightUpdate.Causes.BLOCK_CHANGE)
    }


    private fun getNeighbourEvents(section: ChunkSection, cause: ChunkLightUpdate.Causes, events: HashSet<AbstractWorldUpdate>) {
        val down = section.neighbours[Directions.O_DOWN]
        if (down != null && down.light.update) {
            down.light.update = false
            events += ChunkLightUpdate(chunk, down, cause)
        }
        val up = section.neighbours[Directions.O_UP]
        if (up != null && up.light.update) {
            up.light.update = false
            events += ChunkLightUpdate(chunk, up, cause)
        }


        var neighbourIndex = 0
        for (chunkX in -1..1) {
            for (chunkZ in -1..1) {
                val offset = ChunkPosition(chunkX, chunkZ)
                if (offset == ChunkPosition.EMPTY) continue

                val chunk = chunk.neighbours.array[neighbourIndex++]
                for (chunkY in -1..1) {
                    val neighbourSection = chunk?.get(section.height + chunkY) ?: continue
                    if (!neighbourSection.light.update) {
                        continue
                    }
                    neighbourSection.light.update = false
                    events += ChunkLightUpdate(chunk, neighbourSection, cause)
                }
            }
        }
    }

    fun fireLightChange(section: ChunkSection, cause: ChunkLightUpdate.Causes) {
        if (!section.light.update) {
            return
        }
        section.light.update = false

        val events = hashSetOf<AbstractWorldUpdate>()
        events += ChunkLightUpdate(chunk, section, cause)

        if (cause != ChunkLightUpdate.Causes.RECALCULATE) { // do not fire multiple events per section
            getNeighbourEvents(section, cause, events)
        }


        for (event in events) {
            event.fire(chunk.world.session)
        }
    }

    fun fireLightChange(cause: ChunkLightUpdate.Causes) {
        if (!chunk.neighbours.complete) return
        chunk.sections.forEach { section ->
            fireLightChange(section, cause)
        }
    }


    operator fun get(position: InChunkPosition): LightLevel {
        val sectionHeight = position.sectionHeight
        val inSection = position.inSectionPosition

        val light = when (sectionHeight) {
            minSection - 1 -> bottom[inSection]
            maxSection + 1 -> return top[inSection].with(sky = LightLevel.MAX_LEVEL) // top has always sky=15; TODO: only if dimension has skylight?
            else -> chunk[sectionHeight]?.light?.get(inSection) ?: LightLevel.EMPTY
        }

        if (position.y >= heightmap[position]) {
            // set sky=15
            return light.with(sky = LightLevel.MAX_LEVEL)
        }
        return light
    }

    fun recalculate(fireEvent: Boolean = true, cause: ChunkLightUpdate.Causes) {
        bottom.reset()
        top.reset()

        chunk.sections.forEach { it.light.recalculate() }

        sky.calculate()
        if (fireEvent) {
            fireLightChange(cause)
        }
    }

    fun calculate(fireEvent: Boolean = true, cause: ChunkLightUpdate.Causes) {
        chunk.sections.forEach { it.light.calculate() }

        sky.calculate()
        if (fireEvent) {
            fireLightChange(cause)
        }
    }

    fun reset() {
        chunk.sections.forEach { it.light.reset() }
        bottom.reset()
        top.reset()
    }

    fun propagateFromNeighbours(fireEvent: Boolean = true, cause: ChunkLightUpdate.Causes) {
        chunk.sections.forEach { it.light.propagateFromNeighbours() }

        if (fireEvent) {
            fireLightChange(cause)
        }
    }
}
