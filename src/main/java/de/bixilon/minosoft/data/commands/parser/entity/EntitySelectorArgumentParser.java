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

import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public abstract class EntitySelectorArgumentParser {
    private static final String[] ENTITY_VALUE_TERMINATORS = {",", " ", "]"};

    public abstract void isParsable(Connection connection, ImprovedStringReader stringReader) throws CommandParseException;

    protected Pair<String, String> readNextArgument(ImprovedStringReader stringReader) {
        Pair<String, String> match = stringReader.readUntil(ENTITY_VALUE_TERMINATORS);
        if (!match.value.equals(" ")) {
            // set pointer to --
            stringReader.skip(-1);
        }
        return match;
    }
}
