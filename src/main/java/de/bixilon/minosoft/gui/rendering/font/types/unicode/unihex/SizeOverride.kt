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

package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt

data class SizeOverride(
    val from: Int,
    val to: Int,
    val left: Int,
    val right: Int,
) {

    companion object {

        private fun Any?.unicode(): Int {
            val string = this!!.toString()
            val iterator = string.codePoints().iterator()

            if (!iterator.hasNext()) throw IllegalArgumentException("Not a unicode string $this")
            val code = iterator.nextInt()
            if (iterator.hasNext()) throw IllegalArgumentException("Not a unicode string $this")

            return code
        }

        fun deserialize(data: JsonObject): SizeOverride? {
            val override = SizeOverride(
                from = data["from"].unicode(),
                to = data["to"].unicode(),
                left = data["to"].toInt(),
                right = data["to"].toInt(),
            )
            if (override.to < override.from) return null
            if (override.to - override.from <= 0) return null

            return override
        }

        fun deserialize(data: List<JsonObject>): List<SizeOverride>? {
            val list: MutableList<SizeOverride> = mutableListOf()

            for (override in data) {
                list += deserialize(override) ?: continue
            }

            if (list.isEmpty()) return null

            return list
        }
    }
}
