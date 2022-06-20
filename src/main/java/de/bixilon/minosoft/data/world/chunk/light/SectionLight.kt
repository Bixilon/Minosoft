/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.getIndex
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class SectionLight(
    val section: ChunkSection,
    var light: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION) { 0x00.toByte() }, // packed (skyLight: 0xF0, blockLight: 0x0F)
) : AbstractSectionLight() {

    fun onBlockChange(x: Int, y: Int, z: Int, previous: BlockState?, now: BlockState?) {
        if (previous?.luminance == now?.luminance && previous?.isSolid == now?.isSolid) {
            // no change for light data
            return
        }
        val previousLuminance = previous?.luminance ?: 0
        val luminance = now?.luminance ?: 0

        if (luminance > previousLuminance) {
            onLightIncrease(x, y, z, luminance)
        } else {
            onLightDecrease(x, y, z, luminance)
        }

    }


    fun onLightDecrease(x: Int, y: Int, z: Int, luminance: Byte) {
        // ToDo: make faster, set light to 0 and trace to next night increase. then backtrace
        recalculate()
    }

    fun onLightIncrease(x: Int, y: Int, z: Int, luminance: Byte) {
        traceLightIncrease(x, y, z, luminance)
    }


    private fun traceLightIncrease(x: Int, y: Int, z: Int, nextLuminance: Byte) {
        val index = getIndex(x, y, z)
        val block = section.blocks.unsafeGet(index)
        val blockLuminance = block?.luminance ?: 0
        if (block != null && block.isSolid && blockLuminance == 0.toByte()) {
            // light can not pass through the block
            return
        }

        // get block or next luminance level
        var luminance = nextLuminance
        if (blockLuminance > luminance) {
            luminance = blockLuminance
        }
        val currentLight = light[index].toInt() and 0x0F // we just care about block light
        if (currentLight >= luminance) {
            // light is already higher, no need to trace
            return
        }
        light[index] = luminance
        if (!update) {
            update = true
        }

        if (luminance == 1.toByte()) {
            // we can not further increase the light
            return
        }


        if (blockLuminance > nextLuminance) {
            // we only want to set our own light sources
            return
        }
        val neighbours = section.neighbours ?: return

        val neighbourLuminance = (luminance - 1).toByte()

        if (y > 0) {
            traceLightIncrease(x, y - 1, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_DOWN]?.light?.traceLightIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            traceLightIncrease(x, y + 1, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_UP]?.light?.traceLightIncrease(x, 0, z, neighbourLuminance)
        }

        if (z > 0) {
            traceLightIncrease(x, y, z - 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_NORTH]?.light?.traceLightIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceLightIncrease(x, y, z + 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.traceLightIncrease(x, y, 0, neighbourLuminance)
        }

        if (x > 0) {
            traceLightIncrease(x - 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_WEST]?.light?.traceLightIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, neighbourLuminance)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceLightIncrease(x + 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_EAST]?.light?.traceLightIncrease(0, y, z, neighbourLuminance)
        }
    }


    fun recalculate() {
        // clear light
        for (index in light.indices) {
            light[index] = 0x00.toByte()
        }
        val blocks = section.blocks

        blocks.acquire()
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = getIndex(x, y, z)
                    val luminance = blocks.unsafeGet(index)?.luminance ?: continue
                    if (luminance == 0.toByte()) {
                        // block is not emitting light, ignore it
                        continue
                    }
                    traceLightIncrease(x, y, z, luminance)
                }
            }
        }
        blocks.release()
    }


    override operator fun get(index: Int): Byte {
        return light[index]
    }
}
