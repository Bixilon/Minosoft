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

import de.bixilon.minosoft.data.world.chunk.Chunk
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

    internal fun traceIncrease(x: Int, z: Int, nextLuminance: Int) {
        val index = z shl 4 or x
        val currentLight = light[index].toInt() and 0x0F
        if (currentLight >= nextLuminance) {
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
        val neighbourLuminance = nextLuminance - 1

        if (top) {
            chunk.sections?.last()?.light?.traceIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance)
        } else {
            chunk.sections?.first()?.light?.traceIncrease(x, 0, z, neighbourLuminance)
        }

        if (z > 0) {
            traceIncrease(x, z - 1, neighbourLuminance)
        } else {
            val neighbour = chunk.neighbours?.get(3)
            (if (top) neighbour?.topLight else neighbour?.bottomLight)?.traceIncrease(x, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance)
        }
        if (z < ProtocolDefinition.SECTION_MAX_Y) {
            traceIncrease(x, z + 1, neighbourLuminance)
        } else {
            val neighbour = chunk.neighbours?.get(4)
            (if (top) neighbour?.topLight else neighbour?.bottomLight)?.traceIncrease(x, 0, neighbourLuminance)
        }

        if (x > 0) {
            traceIncrease(x - 1, z, neighbourLuminance)
        } else {
            val neighbour = chunk.neighbours?.get(1)
            (if (top) neighbour?.topLight else neighbour?.bottomLight)?.traceIncrease(ProtocolDefinition.SECTION_MAX_X, z, neighbourLuminance)
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            traceIncrease(x + 1, z, neighbourLuminance)
        } else {
            val neighbour = chunk.neighbours?.get(6)
            (if (top) neighbour?.topLight else neighbour?.bottomLight)?.traceIncrease(0, z, neighbourLuminance)
        }
    }

    fun update(array: ByteArray) {
        // ToDo: Save light from server
    }
}
