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
import de.bixilon.minosoft.data.commands.parser.exceptions.BadSwizzleCombinationCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.Connection

class SwizzleParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun parse(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader): Any? {
        val swizzle = stringReader.readUnquotedString()

        val containing: HashSet<Char> = HashSet()

        for (char in swizzle.toCharArray()) {
            when (char) {
                'x', 'y', 'z' -> {
                    if (containing.contains(char)) {
                        throw BadSwizzleCombinationCommandParseException(stringReader, swizzle)
                    }
                    containing.add(char)
                }
                else -> throw BadSwizzleCombinationCommandParseException(stringReader, swizzle)
            }
        }
        return swizzle

    }

    companion object {
        val SWIZZLE_PARSER = SwizzleParser()
    }
}
