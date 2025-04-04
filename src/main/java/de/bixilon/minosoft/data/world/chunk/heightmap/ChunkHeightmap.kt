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

package de.bixilon.minosoft.data.world.chunk.heightmap

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.*

abstract class ChunkHeightmap(protected val chunk: Chunk) : Heightmap {
    protected val heightmap = IntArray(SECTION_WIDTH_X * SECTION_WIDTH_Z) { Int.MIN_VALUE }

    override fun get(index: Int) = heightmap[index]
    override fun get(x: Int, z: Int) = this[(z shl 4) or x]
    override fun get(xz: InChunkPosition) = heightmap[xz.xz]
    override fun get(xz: InSectionPosition) = heightmap[xz.xz]


    protected abstract fun passes(state: BlockState): HeightmapPass
    protected abstract fun onHeightmapUpdate(x: Int, z: Int, previousY: Int, y: Int)


    override fun recalculate() {
        val maxY = (chunk.maxSection + 1) * SECTION_HEIGHT_Y

        chunk.lock.lock()
        for (xz in 0 until SECTION_WIDTH_X * SECTION_WIDTH_Z) {
            trace(InChunkPosition(xz).with(y = maxY), false)
        }
        chunk.lock.unlock()
    }

    private fun trace(position: InChunkPosition, notify: Boolean) {
        val sections = chunk.sections

        var y = Int.MIN_VALUE
        val index = position.xz

        sectionLoop@ for (sectionIndex in (position.y.sectionHeight - chunk.minSection) downTo 0) {
            if (sectionIndex >= sections.size) {
                // starting from above world
                continue
            }
            val section = sections[sectionIndex] ?: continue
            if (section.blocks.isEmpty) continue

            val min = section.blocks.minPosition
            val max = section.blocks.maxPosition

            if (position.x < min.x || position.x > max.x || position.z < min.z || position.z > max.z) continue // out of section


            for (sectionY in max.y downTo min.y) {
                val state = section.blocks[InSectionPosition((sectionY shl 8) or index)] ?: continue
                val pass = passes(state)
                if (pass == HeightmapPass.PASSES) continue

                y = (sectionIndex + chunk.minSection) * ProtocolDefinition.SECTION_HEIGHT_Y + sectionY
                if (pass == HeightmapPass.ABOVE) y++

                break@sectionLoop
            }
        }
        val previous = heightmap[index]

        if (previous == y) return

        heightmap[index] = y

        if (notify) {
            onHeightmapUpdate(position.x, position.z, previous, y)
        }
    }


    override fun onBlockChange(position: InChunkPosition, state: BlockState?) {
        chunk.lock.lock()
        val index = position.xz

        val previous = heightmap[index]

        if (previous > position.y + 1) {
            // our block is/was not the highest, ignore everything
            chunk.lock.unlock()
            return
        }
        if (state == null) {
            trace(position, true)
            chunk.lock.unlock()
            return
        }

        val next = when (passes(state)) {
            HeightmapPass.ABOVE -> position.y + 1
            HeightmapPass.IN -> position.y
            HeightmapPass.PASSES -> previous
        }

        chunk.lock.unlock()

        if (previous != next) {
            heightmap[index] = next
            onHeightmapUpdate(position.x, position.z, previous, next)
        }
    }

    protected enum class HeightmapPass {
        ABOVE,
        IN,
        PASSES,
        ;
    }
}
