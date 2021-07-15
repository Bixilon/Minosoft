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

package de.bixilon.minosoft.data.commands.parser.entity;

import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.RangeParser;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;

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
        return this.minValue;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public boolean isDecimal() {
        return this.decimal;
    }

    @Override
    public void isParsable(PlayConnection connection, CommandStringReader stringReader, String value) throws CommandParseException {
        RangeParser.readRange(stringReader, value, getMinValue(), getMaxValue(), isDecimal());
    }
}
