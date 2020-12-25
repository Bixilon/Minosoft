/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.ColorNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.protocol.network.Connection

class ColorParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader) {
        val color = stringReader.readUnquotedString()
        try {
            ChatColors.getChatFormattingByName(color)
        } catch (exception: IllegalArgumentException) {
            throw ColorNotFoundCommandParseException(stringReader, color, exception)
        }
    }

    companion object {
        val COLOR_PARSER = ColorParser()
    }
}
