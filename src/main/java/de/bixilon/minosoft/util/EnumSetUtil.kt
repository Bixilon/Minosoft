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

package de.bixilon.minosoft.util

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.unsafe.UnsafeUtil
import org.objenesis.ObjenesisStd
import java.util.*


@Deprecated("kutil 1.24")
object EnumSetUtil {
    private val OBJENSESIS = ObjenesisStd()

    private val ENUM_SET = EnumSet::class.java
    private val ENUM_SET_TYPE = UnsafeUtil.UNSAFE.objectFieldOffset(ENUM_SET.getDeclaredField("elementType"))
    private val ENUM_SET_UNIVERSE = UnsafeUtil.UNSAFE.objectFieldOffset(ENUM_SET.getDeclaredField("universe"))

    private val REGULAR_ENUM_SET = Class.forName("java.util.RegularEnumSet")

    private val JUMBO_ENUM_SET = Class.forName("java.util.RegularEnumSet")
    private val JUMBO_ENUM_SET_ELEMENTS = UnsafeUtil.UNSAFE.objectFieldOffset(JUMBO_ENUM_SET.getDeclaredField("elements"))


    private fun <T : Enum<T>> EnumSet<T>.update(type: Class<T>, universe: Array<T>) {
        UnsafeUtil.UNSAFE.putObject(this, this@EnumSetUtil.ENUM_SET_TYPE, type)
        UnsafeUtil.UNSAFE.putObject(this, this@EnumSetUtil.ENUM_SET_UNIVERSE, universe)
    }

    fun <T : Enum<T>> createRegular(clazz: Class<T>, universe: Array<T>): EnumSet<T> {
        // return  RegularEnumSet(clazz, universe)
        val set = UnsafeUtil.UNSAFE.allocateInstance(REGULAR_ENUM_SET).unsafeCast<EnumSet<T>>()
        set.update(clazz, universe)

        return set.unsafeCast()
    }

    fun <T : Enum<T>> createJumbo(clazz: Class<T>, universe: Array<T>): EnumSet<T> {
        // return JumboEnumSet(clazz, universe)
        val set = OBJENSESIS.newInstance(REGULAR_ENUM_SET).unsafeCast<EnumSet<T>>()
        UnsafeUtil.UNSAFE.putObject(this, this@EnumSetUtil.JUMBO_ENUM_SET_ELEMENTS, LongArray(universe.size + 63 ushr 6))

        set.update(clazz, universe)


        return set
    }

    fun <T : Enum<T>> create(clazz: Class<T>, universe: Array<T>): EnumSet<T> {
        return EnumSet.noneOf(clazz) // TODO: optimize and use universe
        return if (universe.size <= Long.SIZE_BITS) createRegular(clazz, universe) else createJumbo(clazz, universe)
    }
}
