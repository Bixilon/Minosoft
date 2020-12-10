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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class CommandParser {

    public static HashBiMap<ModIdentifier, CommandParser> COMMAND_PARSERS = HashBiMap.create(Map.of(
            new ModIdentifier("brigadier:bool"), BooleanParser.BOOLEAN_PARSER,
            new ModIdentifier("brigadier:double"), DoubleParser.DOUBLE_PARSER,
            new ModIdentifier("brigadier:float"), FloatParser.FLOAT_PARSER,
            new ModIdentifier("brigadier:integer"), IntegerParser.INTEGER_PARSER,
            new ModIdentifier("brigadier:string"), StringParser.STRING_PARSER,
            new ModIdentifier("entity"), EntityParser.ENTITY_PARSER,
            new ModIdentifier("score_holder"), ScoreHolderParser.SCORE_HOLDER_PARSER,
            new ModIdentifier("range"), RangeParser.RANGE_PARSER,
            new ModIdentifier("message"), MessageParser.MESSAGE_PARSER
    ));

    public static CommandParser createInstance(ModIdentifier identifier) {
        return COMMAND_PARSERS.getOrDefault(identifier, DummyParser.DUMMY_PARSER);
    }

    @Nullable
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return null;
    }

    public abstract void isParsable(@Nullable ParserProperties properties, ImprovedStringReader stringReader) throws CommandParseException;

}
