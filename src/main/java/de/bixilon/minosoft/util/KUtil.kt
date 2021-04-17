/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.minosoft.util.enum.AliasableEnum
import java.util.*

object KUtil {

    fun <T : Enum<*>> getEnumValues(values: Array<T>): Map<String, T> {
        val ret: MutableMap<String, T> = mutableMapOf()

        for (value in values) {
            ret[value.name.toLowerCase()] = value

            if (value is AliasableEnum) {
                for (name in value.names) {
                    ret[name] = value
                }
            }
        }

        return ret.toMap()
    }

    fun bitSetOf(long: Long): BitSet {
        return BitSet.valueOf(longArrayOf(long))
    }

    fun <T> Any.unsafeCast(): T {
        return this as T
    }

    inline fun <reified T> Any.nullCast(): T? {
        if (this is T) {
            return this
        }
        return null
    }
}
