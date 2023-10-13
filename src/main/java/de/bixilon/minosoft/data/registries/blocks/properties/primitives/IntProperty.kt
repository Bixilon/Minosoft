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

package de.bixilon.minosoft.data.registries.blocks.properties.primitives

import de.bixilon.kutil.collections.iterator.EmptyIterator.emptyIterator
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty

class IntProperty(
    name: String,
    val range: IntRange? = null,
) : BlockProperty<Int>(name) {

    override fun parse(value: Any): Int {
        val value = value.toInt()

        if (range != null && value !in range) throw IllegalArgumentException("Value out of range: $value (expected=$range)")

        return value
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IntProperty) return false
        if (other.name != name) return false
        return other.range == range
    }

    override fun iterator(): Iterator<Int> {
        return range?.iterator() ?: emptyIterator()
    }
}
