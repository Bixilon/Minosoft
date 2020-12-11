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

import de.bixilon.minosoft.data.EntityClassMappings;
import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exception.entity.UnknownEntityCommandParseException;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class IdentifierSelectorArgumentParser extends EntitySelectorArgumentParser {
    public static final IdentifierSelectorArgumentParser ENTITY_TYPE_IDENTIFIER_SELECTOR_ARGUMENT_PARSER = new IdentifierSelectorArgumentParser();


    @Override
    public void isParsable(Connection connection, ImprovedStringReader stringReader) throws CommandParseException {
        Pair<String, String> match = readNextArgument(stringReader);
        String value = match.key;
        if (match.key.startsWith("!")) {
            value = value.substring(1);
        }
        ModIdentifier identifier = new ModIdentifier(value);
        if (this == ENTITY_TYPE_IDENTIFIER_SELECTOR_ARGUMENT_PARSER) {
            if (!EntityClassMappings.ENTITY_CLASS_MAPPINGS.containsValue(identifier)) {
                throw new UnknownEntityCommandParseException(stringReader, match.key);
            }
            return;
        }
        throw new RuntimeException();
    }
}
