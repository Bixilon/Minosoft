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

import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.number.*;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class RangeSelectorArgumentParser extends EntitySelectorArgumentParser {
    public static final RangeSelectorArgumentParser LEVEL_SELECTOR_ARGUMENT_PARSER = new RangeSelectorArgumentParser(0, Integer.MAX_VALUE, false);
    public static final RangeSelectorArgumentParser DISTANCE_SELECTOR_ARGUMENT_PARSER = new RangeSelectorArgumentParser(0, Integer.MAX_VALUE, true);
    public static final RangeSelectorArgumentParser ROTATION_SELECTOR_ARGUMENT_PARSER = new RangeSelectorArgumentParser(Integer.MIN_VALUE, Integer.MAX_VALUE, true);

    private final int minValue;
    private final int maxValue;
    private final boolean decimal;

    public RangeSelectorArgumentParser(int minValue, int maxValue, boolean decimal) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimal = decimal;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean isDecimal() {
        return decimal;
    }

    @Override
    public void isParsable(Connection connection, ImprovedStringReader stringReader) throws CommandParseException {
        Pair<String, String> match = readNextArgument(stringReader);

        if (match.key.contains("..")) {
            // range
            String[] split = match.key.split("\\.\\.");
            if (split.length != 2) {
                throw new RangeBadFormatCommandParseException(stringReader, match.key);
            }
            double min;
            double max;
            if (split[0].isBlank()) {
                min = getMinValue();
            } else {
                min = parseValue(stringReader, match.key, split[0]);
            }
            if (split[1].isBlank()) {
                max = getMaxValue();
            } else {
                max = parseValue(stringReader, match.key, split[1]);
            }

            if (min < getMinValue() || max > getMaxValue()) {
                throw new ValueOutOfRangeCommandParseException(stringReader, minValue, maxValue, match.key);
            }
            if (min > max) {
                throw new MinimumBiggerAsMaximumCommandParseException(stringReader, match.key);
            }
            return;
        }

        parseValue(stringReader, match.key, match.key);
    }

    private double parseValue(ImprovedStringReader stringReader, String match, String value) throws CommandParseException {
        if (value.contains(".")) {
            if (!isDecimal()) {
                throw new NumberIsDecimalCommandParseException(stringReader, match);
            }
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            if (isDecimal()) {
                throw new DoubleCommandParseException(stringReader, match, e);
            }
            throw new IntegerCommandParseException(stringReader, match, e);
        }
    }
}
