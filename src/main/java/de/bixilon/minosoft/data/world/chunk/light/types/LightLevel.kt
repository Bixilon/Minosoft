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

package de.bixilon.minosoft.data.world.chunk.light.types


import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.data.world.chunk.light.LightUtil.assertLight

@JvmInline
value class LightLevel(val raw: Byte) {

    constructor(block: Int, sky: Int) : this(((block shl BLOCK_SHIFT) or (sky shl SKY_SHIFT)).toByte()) {
        assertLight(block >= MIN_LEVEL)
        assertLight(block <= MAX_LEVEL)

        assertLight(sky >= MIN_LEVEL)
        assertLight(sky <= MAX_LEVEL)
    }

    inline val block: Int get() = (raw.toInt() ushr BLOCK_SHIFT) and BLOCK_MASK
    inline val sky: Int get() = (raw.toInt() ushr SKY_SHIFT) and SKY_MASK

    inline val index: Int get() = raw.toInt() and 0xFF


    inline fun with(block: Int = this.block, sky: Int = this.sky) = LightLevel(block, sky)

    inline fun max(other: LightLevel) = LightLevel(maxOf(block, other.block), maxOf(sky, other.sky))

    inline fun decrease() = LightLevel(maxOf(0, block - 1), maxOf(0, sky - 1))

    override fun toString() = (raw.toInt() and 0xFF).toHex(2)

    companion object {
        const val BITS = 4
        const val BLOCK_SHIFT = 0
        const val BLOCK_MASK = 0x0F
        const val SKY_SHIFT = 4
        const val SKY_MASK = 0x0F


        const val MIN_LEVEL = 0
        const val MAX_LEVEL = (1 shl BITS) - 1

        const val LEVELS = (1 shl BITS)

        val EMPTY = LightLevel(MIN_LEVEL, MIN_LEVEL)
        val MAX = LightLevel(MAX_LEVEL, MAX_LEVEL)
    }
}
