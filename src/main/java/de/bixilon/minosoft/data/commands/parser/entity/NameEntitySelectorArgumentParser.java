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

import de.bixilon.minosoft.data.commands.parser.StringParser;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class NameEntitySelectorArgumentParser implements EntitySelectorArgumentParser {
    public static final NameEntitySelectorArgumentParser NAME_ENTITY_SELECTOR_ARGUMENT_PARSER = new NameEntitySelectorArgumentParser();
    private static final StringParserProperties STRING_PARSER_PROPERTIES = new StringParserProperties(StringParserProperties.StringSettings.QUOTABLE_PHRASE, true);

    @Override
    public boolean isParsable(ImprovedStringReader stringReader) {
        // if it starts with a quote, it will end with a quote
        if (stringReader.get(1).equals("\"")) {
            return StringParser.STRING_PARSER.isParsable(STRING_PARSER_PROPERTIES, stringReader);
        }
        // read until next space or comma
        Pair<String, String> match = stringReader.readUntil(",", " ", "]");
        if (!match.value.equals(" ")) {
            // set pointer to --
            stringReader.skip(-1);
        }
        return true;
    }
}
