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

package de.bixilon.minosoft.data.world.chunk.heightmap

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

abstract class ChunkHeightmap(protected val chunk: Chunk) : Heightmap {
    protected val heightmap = IntArray(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) { Int.MIN_VALUE }

    override fun get(index: Int) = heightmap[index]
    override fun get(x: Int, z: Int) = heightmap[(z shl 4) or x]


    protected abstract fun passes(state: BlockState): HeightmapPass
    protected abstract fun onHeightmapUpdate(x: Int, z: Int, previous: Int, now: Int)


    override fun recalculate() {
        chunk.lock.lock()
        val maxY = (chunk.maxSection + 1) * ProtocolDefinition.SECTION_HEIGHT_Y

        for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
            for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                trace(x, maxY, z, false)
            }
        }
        chunk.lock.unlock()
    }

    private fun trace(x: Int, startY: Int, z: Int, notify: Boolean) {
        val sections = chunk.sections

        var y = Int.MIN_VALUE

        sectionLoop@ for (sectionIndex in (startY.sectionHeight - chunk.minSection) downTo 0) {
            if (sectionIndex >= sections.size) {
                // starting from above world
                continue
            }
            val section = sections[sectionIndex] ?: continue
            if (section.blocks.isEmpty) continue

            val min = section.blocks.minPosition
            val max = section.blocks.maxPosition

            if (x < min.x || x > max.x || z < min.z || z > max.z) continue // out of section

            section.acquire()
            for (sectionY in max.y downTo min.y) {
                val state = section.blocks[x, sectionY, z] ?: continue
                val pass = passes(state)
                if (pass == HeightmapPass.PASSES) continue

                y = (sectionIndex + chunk.minSection) * ProtocolDefinition.SECTION_HEIGHT_Y + sectionY
                if (pass == HeightmapPass.ABOVE) y++

                section.release()
                break@sectionLoop
            }
            section.release()
        }
        val index = (z shl 4) or x
        val previous = heightmap[index]

        if (previous == y) return

        heightmap[index] = y

        if (notify) {
            onHeightmapUpdate(x, z, previous, y)
        }
    }


    override fun onBlockChange(x: Int, y: Int, z: Int, state: BlockState?) {
        chunk.lock.lock()
        val index = (z shl 4) or x

        val previous = heightmap[index]

        if (previous > y + 1) {
            // our block is/was not the highest, ignore everything
            chunk.lock.unlock()
            return
        }
        if (state == null) {
            trace(x, y, z, true)
            chunk.lock.unlock()
            return
        }

        val next = when (passes(state)) {
            HeightmapPass.ABOVE -> y + 1
            HeightmapPass.IN -> y
            HeightmapPass.PASSES -> previous
        }

        chunk.lock.unlock()

        if (previous != next) {
            heightmap[index] = next
            onHeightmapUpdate(x, z, previous, next)
        }
    }

    protected enum class HeightmapPass {
        ABOVE,
        IN,
        PASSES,
        ;
    }
}
