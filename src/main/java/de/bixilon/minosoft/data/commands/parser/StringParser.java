/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.commands.parser;

import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.BlankStringCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import org.apache.commons.lang3.StringUtils;

public class StringParser extends CommandParser {
    public static final StringParser STRING_PARSER = new StringParser();

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new StringParserProperties(buffer);
    }

    @Override
    public Object parse(PlayConnection connection, ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        StringParserProperties stringParserProperties = ((StringParserProperties) properties);
        String string;
        switch (stringParserProperties.getSetting()) {
            case SINGLE_WORD:
                string = stringReader.readUnquotedString();
                break;
            case QUOTABLE_PHRASE:
                string = stringReader.readString();
                break;
            case GREEDY_PHRASE:
                string = stringReader.readRemaining();
                break;
            default:
                throw new IllegalStateException();
        }

        if (!stringParserProperties.isAllowEmptyString() && StringUtils.isBlank(string)) {
            throw new BlankStringCommandParseException(stringReader, string);
        }
        return string;
    }
}
