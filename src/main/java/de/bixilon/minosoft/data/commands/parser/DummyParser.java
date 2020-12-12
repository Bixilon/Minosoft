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
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

import javax.annotation.Nullable;

@Deprecated
public class DummyParser extends CommandParser {
    public static final DummyParser DUMMY_PARSER = new DummyParser();

    @Override
    public void isParsable(Connection connection, @Nullable ParserProperties properties, ImprovedStringReader stringReader) throws CommandParseException {
    }
}
