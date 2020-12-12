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

package de.bixilon.minosoft.data.commands.parser.entity;

import de.bixilon.minosoft.data.commands.parser.StringParser;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class StringSelectorArgumentParser extends EntitySelectorArgumentParser {
    public static final StringSelectorArgumentParser STRING_SELECTOR_ARGUMENT_PARSER = new StringSelectorArgumentParser();
    private static final StringParserProperties STRING_PARSER_PROPERTIES = new StringParserProperties(StringParserProperties.StringSettings.QUOTABLE_PHRASE, true);

    @Override
    public void isParsable(Connection connection, ImprovedStringReader stringReader) throws CommandParseException {
        // if it starts with a quote, it will end with a quote
        if (stringReader.get(1).equals("\"")) {
            StringParser.STRING_PARSER.isParsable(connection, STRING_PARSER_PROPERTIES, stringReader);
            return;
        }
        readNextArgument(stringReader);
    }
}
