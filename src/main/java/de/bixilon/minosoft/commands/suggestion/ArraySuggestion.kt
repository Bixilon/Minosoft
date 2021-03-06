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

package de.bixilon.minosoft.commands.suggestion

import de.bixilon.minosoft.commands.suggestion.types.SuggestionType

class ArraySuggestion<T>(val values: Collection<T>, val ignoreCase: Boolean = false) : SuggestionType<T> {

    override fun suggest(input: String?): Collection<T>? {
        if (input == null || input.isBlank()) {
            return values
        }
        val list: MutableList<T> = mutableListOf()
        for (entry in values) {
            if (entry.toCasedString().startsWith(input.toCasedString())) {
                list += entry
            }
        }
        if (list.isEmpty()) {
            return null
        }
        return list
    }

    private fun Any?.toCasedString(): String {
        var string = this.toString()
        if (ignoreCase) {
            string = string.lowercase()
        }
        return string
    }
}
