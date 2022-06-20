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

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

@Deprecated("ToDo")
class BorderSectionLight(val top: Boolean) : AbstractSectionLight() {
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
}
