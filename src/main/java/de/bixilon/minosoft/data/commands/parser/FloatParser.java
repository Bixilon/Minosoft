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
import de.bixilon.minosoft.data.commands.parser.exceptions.number.ValueOutOfRangeCommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.FloatParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class FloatParser extends CommandParser {
    public static final FloatParser FLOAT_PARSER = new FloatParser();

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new FloatParserProperties(buffer);
    }

    @Override
    public Object parse(PlayConnection connection, ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        float value = stringReader.readFloat();
        FloatParserProperties floatParserProperties = (FloatParserProperties) properties;
        if (value < floatParserProperties.getMinValue() || value > floatParserProperties.getMaxValue()) {
            throw new ValueOutOfRangeCommandParseException(stringReader, floatParserProperties.getMinValue(), floatParserProperties.getMaxValue(), value);
        }
        return value;
    }
}
