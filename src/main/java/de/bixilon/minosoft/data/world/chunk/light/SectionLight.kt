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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.getIndex
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
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
            startDecreaseTrace(x, y, z, luminance)
        }
    }


    /*
    TODO: Light decrease
     * Go in every direction and check if the light is different, else do nothing
     * if the light is equal or 1 more of the neighbour, then there is another light source
     * we will set our own light the  current neighbour level and check if any neighbour has a higher level
     * (before that we will need to decrease all decreasing light levels, to not confuse the recursive algorithm)
     * should work, I guess?
     */

    private fun startDecreaseTrace(x: Int, y: Int, z: Int, luminance: Byte) {
        traceDecrease(x, y, z, luminance, (ProtocolDefinition.MAX_LIGHT_LEVEL + 1).toByte(), Vec3i.EMPTY) // 16: invalid light level
    }

    fun traceDecrease(x: Int, y: Int, z: Int, expectedLuminance: Byte, previous: Byte, direction: Vec3i): Byte {
        val index = getIndex(x, y, z)
        val light = light[index]
        if (light == expectedLuminance) {
            return expectedLuminance
        }
        if (light >= previous) {
            // another (stronger!) light source is emitting light here
            return light
        }
        val neighbours = section.neighbours ?: return 0

        update = true
        this.light[index] = expectedLuminance

        val expectedNeighbourLevel = if (expectedLuminance <= 1) 0 else (expectedLuminance - 1).toByte()

        var highestLevel = expectedLuminance

        if (direction.x <= 0) {
            highestLevel = maxOf(
                highestLevel, (if (x > 0) {
                    traceDecrease(x - 1, y, z, expectedNeighbourLevel, light, Vec3i(-1, direction.y, direction.z))
                } else {
                    neighbours[Directions.O_WEST]?.light?.traceDecrease(ProtocolDefinition.SECTION_MAX_X, y, z, expectedLuminance, previous, Vec3i(-1, direction.y, direction.z)) ?: 0
                } - 1).toByte()
            )
        }
        if (direction.x >= 0) {
            highestLevel = maxOf(
                highestLevel, (if (x < ProtocolDefinition.SECTION_MAX_X) {
                    traceDecrease(x + 1, y, z, expectedNeighbourLevel, light, Vec3i(1, direction.y, direction.z))
                } else {
                    neighbours[Directions.O_EAST]?.light?.traceDecrease(0, y, z, expectedNeighbourLevel, light, Vec3i(1, direction.y, direction.z)) ?: 0
                } - 1).toByte()
            )
        }
        if (direction.y <= 0) {
            highestLevel = maxOf(
                highestLevel, (if (y > 0) {
                    traceDecrease(x, y - 1, z, expectedNeighbourLevel, light, Vec3i(direction.x, -1, direction.z))
                } else {
                    neighbours[Directions.O_DOWN]?.light?.traceDecrease(x, ProtocolDefinition.SECTION_MAX_Y, z, expectedNeighbourLevel, light, Vec3i(direction.x, -1, direction.z)) ?: 0
                } - 1).toByte()
            )
        }
        if (direction.y >= 0) {
            highestLevel = maxOf(
                highestLevel, (if (y < ProtocolDefinition.SECTION_MAX_Y) {
                    traceDecrease(x, y + 1, z, expectedNeighbourLevel, light, Vec3i(direction.x, 1, direction.z))
                } else {
                    neighbours[Directions.O_UP]?.light?.traceDecrease(x, 0, z, expectedNeighbourLevel, light, Vec3i(direction.x, 1, direction.z)) ?: 0
                } - 1).toByte()
            )
        }
        if (direction.z <= 0) {
            highestLevel = maxOf(
                highestLevel, (if (z > 0) {
                    traceDecrease(x, y, z - 1, expectedNeighbourLevel, light, Vec3i(direction.x, direction.y, -1))
                } else {
                    neighbours[Directions.O_NORTH]?.light?.traceDecrease(x, y, ProtocolDefinition.SECTION_MAX_Z, expectedNeighbourLevel, light, Vec3i(direction.x, direction.y, -1)) ?: 0
                } - 1).toByte()
            )
        }
        if (direction.z >= 0) {
            highestLevel = maxOf(
                highestLevel, (if (z < ProtocolDefinition.SECTION_MAX_Z) {
                    traceDecrease(x, y, z + 1, expectedNeighbourLevel, light, Vec3i(direction.x, direction.y, 1))
                } else {
                    neighbours[Directions.O_SOUTH]?.light?.traceDecrease(x, y, 0, expectedNeighbourLevel, light, Vec3i(direction.x, direction.y, 1)) ?: 0
                } - 1).toByte()
            )
        }

        this.light[index] = highestLevel


        return highestLevel
    }


    private fun onLightIncrease(x: Int, y: Int, z: Int, nextLuminance: Byte) {
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
        val currentLight = light[index].toInt() // and 0x0F // we just care about block light
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
            onLightIncrease(x, y - 1, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_DOWN]?.light?.onLightIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance)
        }
        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            onLightIncrease(x, y + 1, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_UP]?.light?.onLightIncrease(x, 0, z, neighbourLuminance)
        }

        if (z > 0) {
            onLightIncrease(x, y, z - 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_NORTH]?.light?.onLightIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            onLightIncrease(x, y, z + 1, neighbourLuminance)
        } else {
            neighbours[Directions.O_SOUTH]?.light?.onLightIncrease(x, y, 0, neighbourLuminance)
        }

        if (x > 0) {
            onLightIncrease(x - 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_WEST]?.light?.onLightIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, neighbourLuminance)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            onLightIncrease(x + 1, y, z, neighbourLuminance)
        } else {
            neighbours[Directions.O_EAST]?.light?.onLightIncrease(0, y, z, neighbourLuminance)
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
                    onLightIncrease(x, y, z, luminance)
                }
            }
        }
        blocks.release()
    }


    override operator fun get(index: Int): Byte {
        return light[index]
    }
}
