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

import de.bixilon.kutil.array.ArrayUtil.getFirst
import de.bixilon.kutil.array.ArrayUtil.getLast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class BorderSectionLight(
    val top: Boolean,
    val chunk: Chunk,
) : AbstractSectionLight() {
    val light = ByteArray(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z)

    override fun get(x: Int, y: Int, z: Int): Byte {
        if ((top && y == 0) || (!top && y == ProtocolDefinition.SECTION_MAX_Y)) {
            return light[getIndex(x, z)]
        }
        return 0x00
    }

    override fun get(index: Int): Byte {
        val y = index shr 8

        if ((top && y == 0) || (!top && y == ProtocolDefinition.SECTION_MAX_Y)) {
            return light[index and 0xFF]
        }
        return 0x00.toByte()
    }

    private fun getIndex(x: Int, z: Int): Int {
        return z shl 4 or x
    }

    private fun updateY() {
        // we can not further increase the light
        if (top) {
            chunk.sections?.getLast()?.light?.apply { if (!update) update = true }
        } else {
            chunk.sections?.getFirst()?.light?.apply { if (!update) update = true }
        }
    }

    fun traceBlockIncrease(x: Int, z: Int, nextLuminance: Int) {
        val index = z shl 4 or x
        val currentLight = light[index].toInt() and SectionLight.BLOCK_LIGHT_MASK
        if (currentLight >= nextLuminance) {
            // light is already higher, no need to trace
            return
        }
        this.light[index] = ((this.light[index].toInt() and SectionLight.SKY_LIGHT_MASK) or nextLuminance).toByte()

        if (!update) {
            update = true
        }


        if (nextLuminance == 1) {
            return updateY()
        }
        val neighbourLuminance = nextLuminance - 1

        if (top) {
            chunk.sections?.getLast()?.light?.traceBlockIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance, Directions.DOWN)
        } else {
            chunk.sections?.getFirst()?.light?.traceBlockIncrease(x, 0, z, neighbourLuminance, Directions.UP)
        }

        if (z > 0) {
            traceBlockIncrease(x, z - 1, neighbourLuminance)
        } else {
            chunk.neighbours.get(ChunkNeighbours.NORTH)?.getBorderLight()?.traceBlockIncrease(x, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceBlockIncrease(x, z + 1, neighbourLuminance)
        } else {
            chunk.neighbours.get(ChunkNeighbours.SOUTH)?.getBorderLight()?.traceBlockIncrease(x, 0, neighbourLuminance)
        }

        if (x > 0) {
            traceBlockIncrease(x - 1, z, neighbourLuminance)
        } else {
            chunk.neighbours.get(ChunkNeighbours.WEST)?.getBorderLight()?.traceBlockIncrease(ProtocolDefinition.SECTION_MAX_X, z, neighbourLuminance)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceBlockIncrease(x + 1, z, neighbourLuminance)
        } else {
            chunk.neighbours.get(ChunkNeighbours.EAST)?.getBorderLight()?.traceBlockIncrease(0, z, neighbourLuminance)
        }
    }

    private fun Chunk.getBorderLight(): BorderSectionLight {
        return if (top) light.top else light.bottom
    }

    fun traceSkyIncrease(x: Int, z: Int, nextLevel: Int) {
        // TODO: check heightmap
        val index = z shl 4 or x
        val light = light[index].toInt()
        if ((light and SectionLight.SKY_LIGHT_MASK shr 4) >= nextLevel) {
            // light is already higher, no need to trace
            return
        }
        this.light[index] = ((light and SectionLight.BLOCK_LIGHT_MASK) or (nextLevel shl 4)).toByte()

        if (!update) {
            update = true
        }


        if (nextLevel <= 1) {
            return updateY()
        }
        val neighbourLevel = nextLevel - 1

        if (top) {
            chunk.sections?.getLast()?.light?.traceSkylightIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLevel, Directions.DOWN, chunk.maxSection * ProtocolDefinition.SECTION_HEIGHT_Y + ProtocolDefinition.SECTION_MAX_Y)
        } else {
            chunk.sections?.getFirst()?.light?.traceSkylightIncrease(x, 0, z, neighbourLevel, Directions.UP, chunk.minSection * ProtocolDefinition.SECTION_HEIGHT_Y)
        }

        if (z > 0) {
            traceSkyIncrease(x, z - 1, neighbourLevel)
        } else {
            chunk.neighbours.get(ChunkNeighbours.NORTH)?.getBorderLight()?.traceSkyIncrease(x, ProtocolDefinition.SECTION_MAX_Z, neighbourLevel)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceSkyIncrease(x, z + 1, neighbourLevel)
        } else {
            chunk.neighbours.get(ChunkNeighbours.SOUTH)?.getBorderLight()?.traceSkyIncrease(x, 0, neighbourLevel)
        }

        if (x > 0) {
            traceSkyIncrease(x - 1, z, neighbourLevel)
        } else {
            chunk.neighbours.get(ChunkNeighbours.WEST)?.getBorderLight()?.traceSkyIncrease(ProtocolDefinition.SECTION_MAX_X, z, neighbourLevel)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceSkyIncrease(x + 1, z, neighbourLevel)
        } else {
            chunk.neighbours.get(ChunkNeighbours.EAST)?.getBorderLight()?.traceSkyIncrease(0, z, neighbourLevel)
        }
    }

    internal fun decreaseCheckLevel(x: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckX(z, light, reset)
        val neighbours = chunk.neighbours.get() ?: return

        if (x - light < 0) {
            neighbours[ChunkNeighbours.WEST].getBorderLight().decreaseCheckX(z, light - x, reset)
        }
        if (x + light > ProtocolDefinition.SECTION_MAX_X) {
            neighbours[ChunkNeighbours.EAST].getBorderLight().decreaseCheckX(z, light - (ProtocolDefinition.SECTION_MAX_X - x), reset)
        }
    }

    private fun decreaseCheckX(z: Int, light: Int, reset: Boolean) {
        val neighbours = chunk.neighbours.get() ?: return
        if (reset) reset()

        if (z - light < 0) {
            val neighbour = neighbours[ChunkNeighbours.NORTH].getBorderLight()
            if (reset) neighbour.reset()
        }
        if (z + light > ProtocolDefinition.SECTION_MAX_Z) {
            val neighbour = neighbours[ChunkNeighbours.SOUTH].getBorderLight()
            if (reset) neighbour.reset()
        }
    }

    fun reset() {
        for (i in light.indices) {
            light[i] = 0x00
        }
    }

    fun update(array: ByteArray) {
        // ToDo: Save light from server
    }
}
