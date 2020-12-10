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

import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class StringParser extends CommandParser {
    public static final StringParser STRING_PARSER = new StringParser();

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new StringParserProperties(buffer);
    }

    @Override
    public boolean isParsable(ParserProperties properties, ImprovedStringReader stringReader) {
        // Why the hell can't I use yield here :?
        StringParserProperties stringParserProperties = ((StringParserProperties) properties);
        switch (stringParserProperties.getSetting()) {
            case SINGLE_WORD -> {
                String rest = stringReader.readUntilNextCommandArgument();
                if (stringParserProperties.isAllowEmptyString()) {
                    return true;
                }
                return !rest.isBlank();
            }
            case QUOTABLE_PHRASE -> {
                if (stringReader.get(1).equals("\"")) {
                    stringReader.skip(1);
                    StringBuilder builder = new StringBuilder();
                    builder.append(stringReader.readUntil("\"").key);
                    String currentString = builder.toString();
                    while (currentString.endsWith("\\") && !currentString.endsWith("\\\\")) {
                        // quotes are escaped, continue
                        builder.append("\"");
                        builder.append(stringReader.readUntil("\""));
                        currentString = builder.toString();
                    }
                    if (stringParserProperties.isAllowEmptyString()) {
                        return true;
                    }
                    return currentString.isBlank();
                }
                if (stringParserProperties.isAllowEmptyString()) {
                    return true;
                }
                return !stringReader.readUntilNextCommandArgument().isBlank();
            }
            case GREEDY_PHRASE -> {
                String rest = stringReader.readRest();
                if (stringParserProperties.isAllowEmptyString()) {
                    return true;
                }
                return !rest.isBlank();
            }
        }
        return false;
    }
}
