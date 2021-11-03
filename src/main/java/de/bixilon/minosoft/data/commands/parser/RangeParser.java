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
import de.bixilon.minosoft.data.commands.parser.arguments.DoubleRange;
import de.bixilon.minosoft.data.commands.parser.arguments.Range;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.*;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.RangeParserProperties;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import org.apache.commons.lang3.StringUtils;

public class RangeParser extends CommandParser {
    public static final RangeParser RANGE_PARSER = new RangeParser();

    public static Range<?> readRange(CommandStringReader stringReader, String argument, double minValue, double maxValue, boolean allowDecimal) throws CommandParseException {
        if (argument.contains("..")) {
            // range
            String[] split = argument.split("\\.\\.");
            if (split.length != 2) {
                throw new RangeBadFormatCommandParseException(stringReader, argument);
            }
            double from;
            double to;
            if (StringUtils.isBlank(split[0])) {
                from = minValue;
            } else {
                from = parseValue(stringReader, argument, split[0], allowDecimal);
            }
            if (StringUtils.isBlank(split[1])) {
                to = maxValue;
            } else {
                to = parseValue(stringReader, argument, split[1], allowDecimal);
            }

            if (from < minValue || to > maxValue) {
                throw new ValueOutOfRangeCommandParseException(stringReader, minValue, maxValue, argument);
            }
            if (from > to) {
                throw new MinimumBiggerAsMaximumCommandParseException(stringReader, argument);
            }
            return new DoubleRange(from, to);
        }
        double value = parseValue(stringReader, argument, argument, allowDecimal);
        return new DoubleRange(value, value);
    }

    private static double parseValue(CommandStringReader stringReader, String match, String value, boolean allowDecimal) throws CommandParseException {
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
    public Object parse(PlayConnection connection, ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        return readRange(stringReader, stringReader.readString(), Double.MIN_VALUE, Double.MAX_VALUE, ((RangeParserProperties) properties).isAllowDecimals());
    }
}
