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

package de.bixilon.minosoft.data.text.formatting

import java.util.*

class TextFormatting(
    val bits: BitSet = BitSet(FormattingCodes.VALUES.size),
) : Iterable<FormattingCodes> {

    operator fun minusAssign(code: FormattingCodes) {
        this[code] = false
    }

    operator fun plusAssign(code: FormattingCodes) {
        this[code] = true
    }

    operator fun set(code: FormattingCodes, value: Boolean) {
        bits[code.ordinal] = value
    }

    fun clear() {
        bits.clear()
    }

    operator fun contains(code: FormattingCodes): Boolean {
        return bits[code.ordinal]
    }

    fun copy(): TextFormatting {
        return TextFormatting(bits.clone() as BitSet)
    }

    fun collect(): Set<FormattingCodes> {
        val set: MutableSet<FormattingCodes> = mutableSetOf()
        for (code in FormattingCodes.VALUES) {
            if (code !in this) continue

            set += code
        }

        return set
    }

    override fun hashCode(): Int {
        return bits.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TextFormatting) return false
        return bits == other.bits
    }

    override fun iterator(): Iterator<FormattingCodes> {
        return TextFormattingIterator()
    }

    private inner class TextFormattingIterator : Iterator<FormattingCodes> {
        private var index = 0

        override fun hasNext(): Boolean {
            return bits.length() > index
        }

        override fun next(): FormattingCodes {
            return FormattingCodes.VALUES[index++]
        }
    }
}
