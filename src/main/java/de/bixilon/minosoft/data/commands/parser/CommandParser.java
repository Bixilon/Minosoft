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
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import javax.annotation.Nullable;

public abstract class CommandParser {
    @Deprecated
    private static final DummyParser DUMMY_PARSER = new DummyParser();
    public static HashBiMap<ModIdentifier, CommandParser> COMMAND_PARSERS = HashBiMap.create();

    static {
        COMMAND_PARSERS.put(new ModIdentifier("brigadier:bool"), new BooleanParser());
        COMMAND_PARSERS.put(new ModIdentifier("brigadier:double"), new DoubleParser());
        COMMAND_PARSERS.put(new ModIdentifier("brigadier:float"), new FloatParser());
        COMMAND_PARSERS.put(new ModIdentifier("brigadier:integer"), new IntegerParser());
        COMMAND_PARSERS.put(new ModIdentifier("brigadier:string"), new StringParser());
        COMMAND_PARSERS.put(new ModIdentifier("entity"), new EntityParser());
        COMMAND_PARSERS.put(new ModIdentifier("score_holder"), new ScoreHolderParser());
        COMMAND_PARSERS.put(new ModIdentifier("range"), new RangeParser());
    }

    public static CommandParser createInstance(ModIdentifier identifier) {
        return COMMAND_PARSERS.getOrDefault(identifier, DUMMY_PARSER);
    }

    @Nullable
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return null;
    }

}
