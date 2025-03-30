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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.kutil.collections.CollectionUtil.mutableBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractBiMap
import de.bixilon.kutil.collections.map.bi.MutableBiMap
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

object ChatColors {
    val BLACK = RGBAColor(0, 0, 0)
    val DARK_BLUE = RGBAColor(0, 0, 170)
    val DARK_GREEN = RGBAColor(0, 170, 0)
    val DARK_AQUA = RGBAColor(0, 170, 170)
    val DARK_RED = RGBAColor(170, 0, 0)
    val DARK_PURPLE = RGBAColor(170, 0, 170)
    val GOLD = RGBAColor(255, 170, 0)
    val GRAY = RGBAColor(170, 170, 170)
    val DARK_GRAY = RGBAColor(85, 85, 85)
    val BLUE = RGBAColor(85, 85, 255)
    val GREEN = RGBAColor(85, 255, 85)
    val AQUA = RGBAColor(85, 255, 255)
    val RED = RGBAColor(255, 85, 85)
    val LIGHT_PURPLE = RGBAColor(255, 85, 255)
    val YELLOW = RGBAColor(255, 255, 85)
    val WHITE = RGBAColor(255, 255, 255)


    val VALUES = RGBAArray(16)
    val NAME_MAP: AbstractBiMap<String, RGBAColor>
    val CHAR_MAP = Object2IntOpenHashMap<RGBAColor>(16)


    init {
        val nameMap: MutableBiMap<String, RGBAColor> = mutableBiMapOf()


        var index = 0
        for (field in this::class.java.declaredFields) {
            val color = field.get(null)
            if (color !is RGBAColor) {
                continue
            }
            VALUES[index] = color
            CHAR_MAP[color] = index
            nameMap[field.name.lowercase()] = color
            index++
        }

        NAME_MAP = nameMap

        CHAR_MAP.defaultReturnValue(-1)
    }

    operator fun get(id: Int): RGBAColor {
        return VALUES[id]
    }

    fun getOrNull(id: Int): RGBAColor? {
        return VALUES.getOrNull(id)
    }

    operator fun get(name: String): RGBAColor? {
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

    fun String.toColor(): RGBAColor? {
        if (this.startsWith("#")) {
            return this.rgba()
        }
        return ChatColors[this]
    }
}
