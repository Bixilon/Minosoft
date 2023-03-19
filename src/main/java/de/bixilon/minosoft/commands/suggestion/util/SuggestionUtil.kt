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

package de.bixilon.minosoft.commands.suggestion.util

import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.ReadResult
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable

object SuggestionUtil {

    fun suggest(entries: Collection<*>, offset: Int, input: String?, case: Boolean): List<Suggestion>? {
        if (entries.isEmpty()) return null
        val input = if (case) input else input?.lowercase()
        val suggestions = mutableListOf<Suggestion>()

        for (entry in entries) {
            var name = entry.toString()
            if (!case) name = name.lowercase()

            if (input != null && !name.startsWith(input)) continue // TODO: that should be improved

            val display = if (entry is TextFormattable) entry.toText() else TextComponent(name)

            suggestions += Suggestion(offset, name, ChatComponent.of(display))
        }

        if (suggestions.isEmpty()) return null

        return suggestions
    }

    fun suggest(entries: Collection<*>, result: ReadResult<String?>, case: Boolean): List<Suggestion>? {
        return suggest(entries, result.start, result.result, case)
    }


    fun apply(input: String, suggestion: Suggestion): String {
        var value = input.substring(0, suggestion.offset).trimWhitespaces()
        if (value.isNotEmpty()) {
            value += " "
        }
        value += suggestion.text

        return value
    }
}
