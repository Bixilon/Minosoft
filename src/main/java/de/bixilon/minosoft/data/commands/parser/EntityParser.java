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

import de.bixilon.minosoft.data.commands.parser.entity.*;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.entity.*;
import de.bixilon.minosoft.data.commands.parser.properties.EntityParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

import java.util.HashMap;
import java.util.HashSet;

public class EntityParser extends CommandParser {
    public static final EntityParser ENTITY_PARSER = new EntityParser();
    private static final HashMap<String, EntitySelectorArgumentParser> ENTITY_FILTER_PARAMETER_LIST = new HashMap<>();

    static {
        // ToDo: add all parsers
        ENTITY_FILTER_PARAMETER_LIST.put("name", StringSelectorArgumentParser.STRING_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("team", StringSelectorArgumentParser.STRING_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("limit", IntegerSelectorArgumentParser.INTEGER_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("level", RangeSelectorArgumentParser.LEVEL_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("gamemode", ListSelectorArgumentParser.GAMEMODE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("distance", RangeSelectorArgumentParser.DISTANCE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("x", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("y", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("z", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("sort", ListSelectorArgumentParser.SORT_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("dx", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("dy", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("dz", DoubleSelectorArgumentParser.DOUBLE_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("predicate", StringSelectorArgumentParser.STRING_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("x_rotation", RangeSelectorArgumentParser.ROTATION_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("y_rotation", RangeSelectorArgumentParser.ROTATION_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("tag", StringSelectorArgumentParser.STRING_SELECTOR_ARGUMENT_PARSER);
        ENTITY_FILTER_PARAMETER_LIST.put("type", IdentifierSelectorArgumentParser.ENTITY_TYPE_IDENTIFIER_SELECTOR_ARGUMENT_PARSER);

        // ToDo: advancements, nbt, scores
    }

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new EntityParserProperties(buffer);
    }

    @Override
    public void isParsable(Connection connection, ParserProperties properties, ImprovedStringReader stringReader) throws CommandParseException {
        EntityParserProperties entityParserProperties = (EntityParserProperties) properties;
        if (stringReader.getNextChar() == '@') {
            // selector
            if (entityParserProperties.isOnlySingleEntity()) {
                throw new SingleEntityOnlyEntityCommandParseException(stringReader, String.valueOf(stringReader.getNextChar()));
            }
            stringReader.skip(1); // skip @
            char selectorChar = stringReader.readChar();
            if (selectorChar != 'a' && selectorChar != 'e' && selectorChar != 'p' && selectorChar != 'r' && selectorChar != 's') {
                // only @a, @e, @p, @r and @s possible
                throw new UnknownMassSelectorEntityCommandParseException(stringReader, stringReader.getNextChar());
            }
            if (selectorChar == 'e' && entityParserProperties.isOnlyPlayers()) {
                throw new PlayerOnlyEntityCommandParseException(stringReader, String.valueOf(selectorChar));
            }

            // parse entity selector

            // example: /msg @a[ name = "Bixilon" ] asd
            if (stringReader.getNextChar() != '[') {
                // no meta data given, valid
                return;
            }
            stringReader.skip(1);

            // meta data, parse it!
            HashSet<String> parameters = new HashSet<>();
            while (true) {
                stringReader.skipSpaces();
                String parameterName = stringReader.readUntil("=").getKey().replaceAll("\\s", ""); // ToDo: only remove prefix and suffix spaces!
                if (parameters.contains(parameterName)) {
                    throw new DuplicatedParameterEntityCommandParseException(stringReader, parameterName);
                }
                if (!ENTITY_FILTER_PARAMETER_LIST.containsKey(parameterName)) {
                    throw new UnknownParameterEntityCommandParseException(stringReader, parameterName);
                }

                stringReader.skipSpaces();

                EntitySelectorArgumentParser parser = ENTITY_FILTER_PARAMETER_LIST.get(parameterName);
                parser.isParsable(connection, stringReader);

                stringReader.skipSpaces();
                parameters.add(parameterName);
                char nextChar = stringReader.getNextChar();
                if (nextChar == ']') {
                    stringReader.skip(1);
                    break;
                }
                if (nextChar == ',') {
                    stringReader.skip(1);
                }
            }
            stringReader.skipSpaces();
            return;
        }
        String value = stringReader.readUntilNextCommandArgument();
        try {
            Util.doesStringEqualsRegex(value, ProtocolDefinition.MINECRAFT_NAME_VALIDATOR);
            return;
        } catch (IllegalArgumentException ignored) {
        }

        if (entityParserProperties.isOnlyPlayers()) {
            throw new PlayerOnlyEntityCommandParseException(stringReader, value);
        }
        try {
            Util.getUUIDFromString(value);
            return;
        } catch (Exception ignored) {
        }
        throw new UnknownEntitySelectorCommandParseException(stringReader, value);
    }
}
