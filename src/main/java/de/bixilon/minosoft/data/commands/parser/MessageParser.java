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

package de.bixilon.minosoft.data.commands.parser;

import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import javax.annotation.Nullable;

public class MessageParser extends StringParser {
    public static final MessageParser MESSAGE_PARSER = new MessageParser();
    private static final StringParserProperties STRING_PARSER_PROPERTIES = new StringParserProperties(StringParserProperties.StringSettings.GREEDY_PHRASE, false);

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return null;
    }

    @Override
    public Object parse(PlayConnection connection, @Nullable ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        return super.parse(connection, STRING_PARSER_PROPERTIES, stringReader);
    }
}
