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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.heightmap.FixedHeightmap
import de.bixilon.minosoft.data.world.chunk.heightmap.LightHeightmap
import de.bixilon.minosoft.data.world.chunk.light.ChunkLightUtil.hasSkyLight
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

class ChunkLight(val chunk: Chunk) {
    private val session = chunk.session
    val heightmap = if (chunk.world.dimension.hasSkyLight()) LightHeightmap(chunk) else FixedHeightmap.MAX_VALUE

    val bottom = BorderSectionLight(false, chunk)
    val top = BorderSectionLight(true, chunk)

    val sky = ChunkSkyLight(this)


    fun onBlockChange(position: InChunkPosition, section: ChunkSection, previous: BlockState?, next: BlockState?) {
        heightmap.onBlockChange(position, next)

        section.light.onBlockChange(position.inSectionPosition, previous, next)

        val neighbours = chunk.neighbours.get() ?: return

        fireLightChange(section, position.y.sectionHeight, neighbours)
    }


    fun fireLightChange(section: ChunkSection, sectionHeight: Int, neighbours: Array<Chunk>, fireSameChunkEvent: Boolean = true) {
        if (!section.light.update) {
            return
        }
        section.light.update = false

        val events = hashSetOf<AbstractWorldUpdate>()
        val chunkPosition = chunk.position
        if (fireSameChunkEvent) {
            events += ChunkLightUpdate(chunkPosition, chunk, sectionHeight, true)

            val down = section.neighbours?.get(Directions.O_DOWN)?.light
            if (down != null && down.update) {
                down.update = false
                events += ChunkLightUpdate(chunkPosition, chunk, sectionHeight - 1, false)
            }
            val up = section.neighbours?.get(Directions.O_UP)?.light
            if (up?.update == true) {
                up.update = false
                events += ChunkLightUpdate(chunkPosition, chunk, sectionHeight + 1, false)
            }
        }


        var neighbourIndex = 0
        for (chunkX in -1..1) {
            for (chunkZ in -1..1) {
                val offset = ChunkPosition(chunkX, chunkZ)
                if (offset == ChunkPosition.EMPTY) continue

                val nextPosition = chunkPosition + offset
                val chunk = neighbours[neighbourIndex++]
                for (chunkY in -1..1) {
                    val neighbourSection = chunk[sectionHeight + chunkY] ?: continue
                    if (!neighbourSection.light.update) {
                        continue
                    }
                    neighbourSection.light.update = false
                    events += ChunkLightUpdate(nextPosition, chunk, sectionHeight + chunkY, false)
                }
            }
        }
        for (event in events) event.fire(session)
    }

    fun fireLightChange(sections: Array<ChunkSection?>, fireSameChunkEvent: Boolean) {
        val neighbours = chunk.neighbours.get() ?: return
        for ((index, section) in sections.withIndex()) {
            fireLightChange(section ?: continue, index + chunk.minSection, neighbours, fireSameChunkEvent)
        }
    }


    operator fun get(position: InChunkPosition): Int {
        val sectionHeight = position.sectionHeight
        val inSection = position.inSectionPosition

        val light = when (sectionHeight) {
            chunk.minSection - 1 -> bottom[inSection].toInt()
            chunk.maxSection + 1 -> return top[inSection].toInt() or SectionLight.SKY_LIGHT_MASK // top has always sky=15
            else -> chunk[sectionHeight]?.light?.get(inSection)?.toInt() ?: 0x00
        } and 0xFF

        if (position.y >= heightmap[position]) {
            // set sky=15
            return light or SectionLight.SKY_LIGHT_MASK
        }
        return light
    }

    fun recalculate(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        bottom.reset()
        top.reset()
        val sections = chunk.sections
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.recalculate()
        }
        sky.calculate()
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }

    fun calculate(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        val sections = chunk.sections
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.calculate()
        }
        sky.calculate()
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }

    fun reset() {
        val sections = chunk.sections
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.reset()
        }
        bottom.reset()
        top.reset()
    }

    fun propagateFromNeighbours(fireEvent: Boolean = true, fireSameChunkEvent: Boolean = true) {
        val sections = chunk.sections
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.propagateFromNeighbours()
        }
        if (fireEvent) {
            fireLightChange(sections, fireSameChunkEvent)
        }
    }
}
