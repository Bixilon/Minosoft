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

import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.*;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.RangeParserProperties;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class RangeParser extends CommandParser {
    public static final RangeParser RANGE_PARSER = new RangeParser();

    public static void readRange(ImprovedStringReader stringReader, String argument, double minValue, double maxValue, boolean allowDecimal) throws CommandParseException {
        if (argument.contains("..")) {
            // range
            String[] split = argument.split("\\.\\.");
            if (split.length != 2) {
                throw new RangeBadFormatCommandParseException(stringReader, argument);
            }
            double min;
            double max;
            if (split[0].isBlank()) {
                min = minValue;
            } else {
                min = parseValue(stringReader, argument, split[0], allowDecimal);
            }
            if (split[1].isBlank()) {
                max = maxValue;
            } else {
                max = parseValue(stringReader, argument, split[1], allowDecimal);
            }

            if (min < minValue || max > maxValue) {
                throw new ValueOutOfRangeCommandParseException(stringReader, minValue, maxValue, argument);
            }
            if (min > max) {
                throw new MinimumBiggerAsMaximumCommandParseException(stringReader, argument);
            }
        }
    }

    private static double parseValue(ImprovedStringReader stringReader, String match, String value, boolean allowDecimal) throws CommandParseException {
        if (value.contains(".")) {
            if (!allowDecimal) {
                throw new NumberIsDecimalCommandParseException(stringReader, match);
            }
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            if (allowDecimal) {
                throw new DoubleCommandParseException(stringReader, match, e);
            }
            throw new IntegerCommandParseException(stringReader, match, e);
        }
    }

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new RangeParserProperties(buffer);
    }

    @Override
    public void isParsable(Connection connection, ParserProperties properties, ImprovedStringReader stringReader) throws CommandParseException {
        readRange(stringReader, stringReader.readUntilNextCommandArgument(), Double.MIN_VALUE, Double.MAX_VALUE, ((RangeParserProperties) properties).isAllowDecimals());
    }
}
