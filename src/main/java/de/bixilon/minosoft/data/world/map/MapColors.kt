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

package de.bixilon.minosoft.data.world.map

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb

object MapColors {

    fun deserialize(data: Any) = when (data) {
        is IntArray -> RGBArray(data)
        is Array<*> -> RGBArray(data.size) { data.cast<Array<Any>>()[it].toInt().rgb() }
        is List<*> -> RGBArray(data.size) {
            val string = data.cast<List<Any>>()[it].toString()
            if (string.startsWith("#")) return@RGBArray string.rgb()
            return@RGBArray string.toInt().rgb()
        }

        else -> TODO("Can not deserialize color array: $data")
    }
}
