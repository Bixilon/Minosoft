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
    var light: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION), // packed (skyLight: 0xF0, blockLight: 0x0F)
) : AbstractSectionLight() {

    fun onBlockChange(x: Int, y: Int, z: Int, previous: BlockState?, now: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = now?.luminance ?: 0

        if (previousLuminance == luminance && previous?.isSolid == now?.isSolid) {
            // no change for light data
            return
        }

        if (luminance > previousLuminance) {
            traceIncrease(x, y, z, luminance)
        } else {
            startDecreaseTrace(x, y, z)
        }
    }

    private fun startDecreaseTrace(x: Int, y: Int, z: Int) {
        // that is kind of hacky, but far easier and kind of faster
        val light = this.light[getIndex(x, y, z)].toInt() and 0x0F

        decreaseLight(x, y, z, light, true) // just clear the light
        decreaseLight(x, y, z, light, false) //
    }

    private fun decreaseLight(x: Int, y: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckLevel(x, z, light, reset)

        val neighbours = section.neighbours ?: return
        if (y - light < 0) {
            neighbours[Directions.O_DOWN]?.light?.decreaseCheckLevel(x, z, light - y, reset)
        }
        if (y + light > ProtocolDefinition.SECTION_MAX_Y) {
            neighbours[Directions.O_UP]?.light?.decreaseCheckLevel(x, z, light - (ProtocolDefinition.SECTION_MAX_Y - y), reset)
        }
    }

    private fun decreaseCheckLevel(x: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckX(z, light, reset)
        val neighbours = section.neighbours ?: return

        if (x - light < 0) {
            neighbours[Directions.O_WEST]?.light?.decreaseCheckX(z, light - x, reset)
        }
        if (x + light > ProtocolDefinition.SECTION_MAX_X) {
            neighbours[Directions.O_EAST]?.light?.decreaseCheckX(z, light - (ProtocolDefinition.SECTION_MAX_X - x), reset)
        }
    }

    private fun decreaseCheckX(z: Int, light: Int, reset: Boolean) {
        val neighbours = section.neighbours ?: return
        if (reset) resetLight() else calculate()

        if (z - light < 0) {
            val neighbour = neighbours[Directions.O_NORTH]?.light
            if (reset) neighbour?.resetLight() else neighbour?.calculate()
        }
        if (z + light > ProtocolDefinition.SECTION_MAX_Z) {
            val neighbour = neighbours[Directions.O_SOUTH]?.light
            if (reset) neighbour?.resetLight() else neighbour?.calculate()
        }
    }

    fun traceIncrease(x: Int, y: Int, z: Int, nextLuminance: Int) {
        val index = getIndex(x, y, z)
        val block = section.blocks.unsafeGet(index)
        val blockLuminance = block?.luminance ?: 0
        if (block != null && block.isSolid && blockLuminance == 0) {
            // light can not pass through the block
            return
        }

        // get block or next luminance level
        val currentLight = light[index].toInt() and 0x0F // we just care about block light
        if (currentLight >= nextLuminance) {
            // light is already higher, no need to trace
            return
        }
        this.light[index] = ((this.light[index].toInt() and 0xF0) or nextLuminance).toByte()
        if (!update) {
            update = true
        }
        val neighbours = section.neighbours ?: return

        if (nextLuminance == 1) {
            // we can not further increase the light
            // set neighbour update, cullface might change lighting properties
            if (y == 0) neighbours[Directions.O_DOWN]?.light?.update = true
            if (y == ProtocolDefinition.SECTION_MAX_Y) neighbours[Directions.O_UP]?.light?.update = true
            if (z == 0) neighbours[Directions.O_NORTH]?.light?.update = true
            if (z == ProtocolDefinition.SECTION_MAX_Z) neighbours[Directions.O_SOUTH]?.light?.update = true
            if (x == 0) neighbours[Directions.O_WEST]?.light?.update = true
            if (x == ProtocolDefinition.SECTION_MAX_X) neighbours[Directions.O_EAST]?.light?.update = true
            return
        }


        if (blockLuminance > nextLuminance) {
            // we only want to set our own light sources
            return
        }

        val neighbourLuminance = nextLuminance - 1

        if (y > 0) {
            traceIncrease(x, y - 1, z, neighbourLuminance)
        } else if (section.sectionHeight == section.chunk?.lowestSection) {
            section.chunk?.bottomLight?.traceIncrease(x, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_DOWN]?.light?.traceIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            traceIncrease(x, y + 1, z, neighbourLuminance)
        } else if (section.sectionHeight == section.chunk?.highestSection) {
            section.chunk?.topLight?.traceIncrease(x, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_UP]?.light?.traceIncrease(x, 0, z, neighbourLuminance)
        }

        if (z > 0) {
            traceIncrease(x, y, z - 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_NORTH]?.light?.traceIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceIncrease(x, y, z + 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.traceIncrease(x, y, 0, neighbourLuminance)
        }

        if (x > 0) {
            traceIncrease(x - 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_WEST]?.light?.traceIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, neighbourLuminance)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceIncrease(x + 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_EAST]?.light?.traceIncrease(0, y, z, neighbourLuminance)
        }
    }

    fun resetLight() {
        for (index in light.indices) {
            light[index] = 0x00.toByte()
        }
    }


    fun recalculate() {
        update = true
        resetLight()
        calculate()
    }

    fun calculate() {
        val blocks = section.blocks

        blocks.acquire()
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = getIndex(x, y, z)
                    val luminance = blocks.unsafeGet(index)?.luminance ?: continue
                    if (luminance == 0) {
                        // block is not emitting light, ignore it
                        continue
                    }
                    traceIncrease(x, y, z, luminance)
                }
            }
        }
        blocks.release()
    }


    override operator fun get(index: Int): Byte {
        return light[index]
    }
}
