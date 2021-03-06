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

package de.bixilon.minosoft.util.nbt.tag

import de.bixilon.kutil.cast.CastUtil.nullCast

object NBTUtil {

    fun compound(): MutableMap<String, Any> {
        return mutableMapOf()
    }


    fun MutableMap<String, Any>.remove(vararg keys: String): Any? {
        for (key in keys) {
            remove(key)?.let { return it }
        }
        return null
    }

    fun <T> Any?.listCast(): MutableList<T>? {
        return this.nullCast()
    }

    operator fun Map<String, Any>.get(vararg keys: String): Any? {
        for (key in keys) {
            this[key]?.let { return it }
        }
        return null
    }

    val Any?.nbtType: NBTTagTypes
        get() {
            return when (this) {
                null -> NBTTagTypes.END
                is Byte -> NBTTagTypes.BYTE
                is Short -> NBTTagTypes.SHORT
                is Int -> NBTTagTypes.INT
                is Long -> NBTTagTypes.LONG
                is Float -> NBTTagTypes.FLOAT
                is Double -> NBTTagTypes.DOUBLE
                is ByteArray -> NBTTagTypes.BYTE_ARRAY
                is CharSequence -> NBTTagTypes.STRING
                is Collection<*> -> NBTTagTypes.LIST
                is Map<*, *> -> NBTTagTypes.COMPOUND
                is IntArray -> NBTTagTypes.INT_ARRAY
                is LongArray -> NBTTagTypes.LONG_ARRAY
                else -> throw IllegalArgumentException("NBT does not support ${this::class.java.name}")
            }
        }
}
