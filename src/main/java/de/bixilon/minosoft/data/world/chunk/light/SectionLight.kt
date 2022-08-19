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
import kotlin.math.abs

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
            traceIncrease(x, y, z, luminance, false)
        } else {
            startDecreaseTrace(x, y, z, luminance)
        }
    }


    /*
    Light decrease
     * Go in every direction and check if the light is different, else do nothing
     * if the light is equal or 1 more of the neighbour, then there is another light source
     * we will set our own light the  current neighbour level and check if any neighbour has a higher level
     * (before that we will need to decrease all decreasing light levels, to not confuse the recursive algorithm)
     * should work, I guess?
     */

    private fun startDecreaseTrace(x: Int, y: Int, z: Int, luminance: Int) {
        val light = this.light[getIndex(x, y, z)].toInt() and 0x0F
        traceDecrease(x, y, z, ProtocolDefinition.MAX_LIGHT_LEVEL + 1, light, 0, 0, 0, 0)
        increaseSources(x, y, z, 0, light, 0, 0, 0)
    }

    fun traceDecrease(x: Int, y: Int, z: Int, previousLight: Int, maxLight: Int, previousPath: Int, directionX: Int, directionY: Int, directionZ: Int) {
        val path = abs(directionX) + abs(directionY) + abs(directionZ)
        if (path < previousPath) {
            // prevent going back
            return
        }
        val index = getIndex(x, y, z)
        val light = light[index].toInt() and 0x0F
        if (light == 0) {
            // already empty, there will be no light in that direction
            return
        }
        if (light > maxLight - path || previousLight <= light) {
            // another (stronger!) light source is emitting light here
            return
        }

        if (path >= maxLight) {
            // max length reached
            return
        }
        if (!update) {
            // the light in our section was updated, needed for the LightChangeEvent
            update = true
        }
        val neighbours = section.neighbours ?: return

        this.light[index] = (this.light[index].toInt() and 0xF0).toByte() // set light level to zero


        if (x > 0) {
            traceDecrease(x - 1, y, z, light, maxLight, path, directionX - 1, directionY, directionZ)
        } else {
            neighbours[Directions.O_WEST]?.light?.traceDecrease(ProtocolDefinition.SECTION_MAX_X, y, z, light, maxLight, path, directionX - 1, directionY, directionZ)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceDecrease(x + 1, y, z, light, maxLight, path, directionX + 1, directionY, directionZ)
        } else {
            neighbours[Directions.O_EAST]?.light?.traceDecrease(0, y, z, light, maxLight, path, directionX + 1, directionY, directionZ)
        }

        if (y > 0) {
            traceDecrease(x, y - 1, z, light, maxLight, path, directionX, directionY - 1, directionZ)
        } else {
            neighbours[Directions.O_DOWN]?.light?.traceDecrease(x, ProtocolDefinition.SECTION_MAX_Y, z, light, maxLight, path, directionX, directionY - 1, directionZ)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            traceDecrease(x, y + 1, z, light, maxLight, path, directionX, directionY + 1, directionZ)
        } else {
            neighbours[Directions.O_UP]?.light?.traceDecrease(x, 0, z, light, maxLight, path, directionX, directionY + 1, directionZ)
        }

        if (z > 0) {
            traceDecrease(x, y, z - 1, light, maxLight, path, directionX, directionY, directionZ - 1)
        } else {
            neighbours[Directions.O_NORTH]?.light?.traceDecrease(x, y, ProtocolDefinition.SECTION_MAX_Z, light, maxLight, path, directionX, directionY, directionZ - 1)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Z) {
            traceDecrease(x, y, z + 1, light, maxLight, path, directionX, directionY, directionZ + 1)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.traceDecrease(x, y, 0, light, maxLight, path, directionX, directionY, directionZ + 1)
        }
    }

    private fun increaseSources(x: Int, y: Int, z: Int, previousPath: Int, maxPath: Int, directionX: Int, directionY: Int, directionZ: Int) {
        val path = abs(directionX) + abs(directionY) + abs(directionZ)
        if (path < previousPath) {
            // prevent going back
            return
        }
        if (path > maxPath) {
            // out of range
            return
        }
        val index = getIndex(x, y, z)
        val light = light[index].toInt() and 0x0F
        if (light > 0) {
            // source is here
            traceIncrease(x, y, z, light, true)
            return
        }

        val neighbours = section.neighbours ?: return


        if (x > 0) {
            increaseSources(x - 1, y, z, path, maxPath, directionX - 1, directionY, directionZ)
        } else {
            neighbours[Directions.O_WEST]?.light?.increaseSources(ProtocolDefinition.SECTION_MAX_X, y, z, path, maxPath, directionX - 1, directionY, directionZ)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            increaseSources(x + 1, y, z, path, maxPath, directionX + 1, directionY, directionZ)
        } else {
            neighbours[Directions.O_EAST]?.light?.increaseSources(0, y, z, path, maxPath, directionX + 1, directionY, directionZ)
        }

        if (y > 0) {
            increaseSources(x, y - 1, z, path, maxPath, directionX, directionY - 1, directionZ)
        } else {
            neighbours[Directions.O_DOWN]?.light?.increaseSources(x, ProtocolDefinition.SECTION_MAX_Y, z, path, maxPath, directionX, directionY - 1, directionZ)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            increaseSources(x, y + 1, z, path, maxPath, directionX, directionY + 1, directionZ)
        } else {
            neighbours[Directions.O_UP]?.light?.increaseSources(x, 0, z, path, maxPath, directionX, directionY + 1, directionZ)
        }

        if (z > 0) {
            increaseSources(x, y, z - 1, path, maxPath, directionX, directionY, directionZ - 1)
        } else {
            neighbours[Directions.O_NORTH]?.light?.increaseSources(x, y, ProtocolDefinition.SECTION_MAX_Z, path, maxPath, directionX, directionY, directionZ - 1)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Z) {
            increaseSources(x, y, z + 1, path, maxPath, directionX, directionY, directionZ + 1)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.increaseSources(x, y, 0, path, maxPath, directionX, directionY, directionZ + 1)
        }
    }


    private fun traceIncrease(x: Int, y: Int, z: Int, nextLuminance: Int, force: Boolean) {
        val index = getIndex(x, y, z)
        val block = section.blocks.unsafeGet(index)
        val blockLuminance = block?.luminance ?: 0
        if (block != null && block.isSolid && blockLuminance == 0) {
            // light can not pass through the block
            return
        }

        // get block or next luminance level
        val currentLight = light[index].toInt() and 0x0F // we just care about block light
        if (currentLight >= nextLuminance && !force) {
            // light is already higher, no need to trace
            return
        }
        this.light[index] = ((this.light[index].toInt() and 0xF0) or nextLuminance).toByte()
        if (!update) {
            update = true
        }

        if (nextLuminance == 1) {
            // we can not further increase the light
            return
        }


        if (blockLuminance > nextLuminance) {
            // we only want to set our own light sources
            return
        }
        val neighbours = section.neighbours ?: return

        val neighbourLuminance = nextLuminance - 1

        if (y > 0) {
            traceIncrease(x, y - 1, z, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_DOWN]?.light?.traceIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance, false)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            traceIncrease(x, y + 1, z, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_UP]?.light?.traceIncrease(x, 0, z, neighbourLuminance, false)
        }

        if (z > 0) {
            traceIncrease(x, y, z - 1, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_NORTH]?.light?.traceIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance, false)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceIncrease(x, y, z + 1, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.traceIncrease(x, y, 0, neighbourLuminance, false)
        }

        if (x > 0) {
            traceIncrease(x - 1, y, z, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_WEST]?.light?.traceIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, neighbourLuminance, false)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceIncrease(x + 1, y, z, neighbourLuminance, false)
        } else {
            neighbours[Directions.O_EAST]?.light?.traceIncrease(0, y, z, neighbourLuminance, false)
        }
    }

    fun resetLight() {
        for (index in light.indices) {
            light[index] = 0x00.toByte()
        }
    }


    fun recalculate() {
        resetLight()
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
                    traceIncrease(x, y, z, luminance, false)
                }
            }
        }
        blocks.release()
    }


    override operator fun get(index: Int): Byte {
        return light[index]
    }
}
