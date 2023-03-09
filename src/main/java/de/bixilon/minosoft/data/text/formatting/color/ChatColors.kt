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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.collections.CollectionUtil.mutableBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractBiMap
import de.bixilon.kutil.collections.map.bi.MutableBiMap
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

object ChatColors {
    @JvmField
    val BLACK = RGBColor(0, 0, 0)

    @JvmField
    val DARK_BLUE = RGBColor(0, 0, 170)

    @JvmField
    val DARK_GREEN = RGBColor(0, 170, 0)

    @JvmField
    val DARK_AQUA = RGBColor(0, 170, 170)

    @JvmField
    val DARK_RED = RGBColor(170, 0, 0)

    @JvmField
    val DARK_PURPLE = RGBColor(170, 0, 170)

    @JvmField
    val GOLD = RGBColor(255, 170, 0)

    @JvmField
    val GRAY = RGBColor(170, 170, 170)

    @JvmField
    val DARK_GRAY = RGBColor(85, 85, 85)

    @JvmField
    val BLUE = RGBColor(85, 85, 255)

    @JvmField
    val GREEN = RGBColor(85, 255, 85)

    @JvmField
    val AQUA = RGBColor(85, 255, 255)

    @JvmField
    val RED = RGBColor(255, 85, 85)

    @JvmField
    val LIGHT_PURPLE = RGBColor(255, 85, 255)

    @JvmField
    val YELLOW = RGBColor(255, 255, 85)

    @JvmField
    val WHITE = RGBColor(255, 255, 255)


    val VALUES: Array<RGBColor>
    val NAME_MAP: AbstractBiMap<String, RGBColor>
    val CHAR_MAP = Object2IntOpenHashMap<RGBColor>(16)


    init {
        val values: Array<RGBColor?> = arrayOfNulls(16)
        val nameMap: MutableBiMap<String, RGBColor> = mutableBiMapOf()


        var index = 0
        for (field in this::class.java.declaredFields) {
            val color = field.get(null)
            if (color !is RGBColor) {
                continue
            }
            values[index] = color
            CHAR_MAP[color] = index
            nameMap[field.name.lowercase()] = color
            index++
        }

        VALUES = values.cast()
        NAME_MAP = nameMap

        CHAR_MAP.defaultReturnValue(-1)
    }

    operator fun get(id: Int): RGBColor {
        return VALUES[id]
    }

    fun getOrNull(id: Int): RGBColor? {
        return VALUES.getOrNull(id)
    }

    operator fun get(name: String): RGBColor? {
        return when (name) {
            "dark_grey" -> DARK_GRAY
            else -> NAME_MAP[name]
        }
    }

    fun getChar(color: RGBColor?): String? {
        if (color == null) return null
        val char = CHAR_MAP.getInt(color)
        if (char < 0) return null

        return Integer.toHexString(char)
    }

    fun String.toColor(): RGBColor? {
        if (this.startsWith("#")) {
            return this.asColor()
        }
        return ChatColors[this]
    }
}
