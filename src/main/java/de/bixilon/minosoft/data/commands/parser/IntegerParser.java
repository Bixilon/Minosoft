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

import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exception.number.IntegerCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exception.number.ValueOutOfRangeCommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.IntegerParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class IntegerParser extends CommandParser {
    public static final IntegerParser INTEGER_PARSER = new IntegerParser();

    public boolean isValidValue(IntegerParserProperties properties, int value) {
        return value >= properties.getMinValue() && value <= properties.getMaxValue();
    }

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new IntegerParserProperties(buffer);
    }

    @Override
    public void isParsable(ParserProperties properties, ImprovedStringReader stringReader) throws CommandParseException {
        String argument = stringReader.readUntilNextCommandArgument();
        try {
            int value = Integer.parseInt(argument);
            IntegerParserProperties integerParserProperties = (IntegerParserProperties) properties;
            if (value < integerParserProperties.getMinValue() && value > integerParserProperties.getMaxValue()) {
                throw new ValueOutOfRangeCommandParseException(stringReader, integerParserProperties.getMinValue(), integerParserProperties.getMaxValue(), value);
            }
        } catch (NumberFormatException exception) {
            throw new IntegerCommandParseException(stringReader, argument, exception);
        }
    }
}
