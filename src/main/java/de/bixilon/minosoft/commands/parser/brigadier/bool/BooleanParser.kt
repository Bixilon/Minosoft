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

package de.bixilon.minosoft.commands.parser.brigadier.bool

import de.bixilon.minosoft.commands.errors.suggestion.NoSuggestionError
import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.suggestion.util.SuggestionUtil
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object BooleanParser : BrigadierParser<Boolean>, ArgumentParserFactory<BooleanParser> {
    override val identifier: ResourceLocation = "brigadier:bool".toResourceLocation()
    override val examples: List<Boolean> = listOf(true, false)

    override fun parse(reader: CommandReader): Boolean {
        return reader.readRequiredBoolean()
    }

    fun StringReader.readBoolean(): Boolean? {
        return when (readUnquotedString()) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }


    fun CommandReader.readRequiredBoolean(): Boolean {
        readResult { readBoolean() }.let { return it.result ?: throw BooleanParseError(this, it) }
    }

    override fun getSuggestions(reader: CommandReader): List<Suggestion> {
        val text = reader.readResult { reader.readUnquotedString() }
        return SuggestionUtil.suggest(examples, text.start, text.result, true) ?: throw NoSuggestionError(reader, text)
    }

    override fun read(buffer: PlayInByteBuffer) = this
}
