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

package de.bixilon.minosoft.util.nbt.tag

object NBTUtil {
    fun MutableMap<String, Any>.getAndRemove(key: String): Any? {
        val value = this[key]
        this.remove(key)
        return value
    }

    fun MutableMap<String, Any>.getAndRemove(vararg keys: String): Any? {
        for (key in keys) {
            getAndRemove(key)?.let { return it }
        }
        return null
    }

    fun Any.compoundCast(): MutableMap<String, Any>? {
        try {
            return this as MutableMap<String, Any>
        } catch (ignored: ClassCastException) {
        }
        return null
    }

    fun <T> Any.listCast(): MutableList<T>? {
        try {
            return this as MutableList<T>
        } catch (ignored: ClassCastException) {
        }
        return null
    }
}
