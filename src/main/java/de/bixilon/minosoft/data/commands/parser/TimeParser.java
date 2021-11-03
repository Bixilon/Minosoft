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
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownTimeUnitCommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;

public class TimeParser extends CommandParser {
    public static final TimeParser TIME_PARSER = new TimeParser();


    @Override
    public Object parse(PlayConnection connection, ParserProperties properties, CommandStringReader stringReader) throws CommandParseException {
        int time = stringReader.readInt();

        if (stringReader.canRead()) {
            char unit = stringReader.read();
            switch (unit) {
                case 'd':
                    time *= 24000;
                    break;
                case 's':
                    time *= 20;
                    break;
                case 't':
                    time *= 1;
                    break;
                case ' ':
                    stringReader.skip(-1);
                    break;
                default:
                    throw new UnknownTimeUnitCommandParseException(stringReader, String.valueOf(unit));
            }
        }
        return time;
    }
}
