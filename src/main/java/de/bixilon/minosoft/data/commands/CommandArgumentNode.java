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

package de.bixilon.minosoft.data.commands;

import de.bixilon.minosoft.data.commands.parser.CommandParser;
import de.bixilon.minosoft.data.commands.parser.CommandParsers;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;

import javax.annotation.Nullable;

public class CommandArgumentNode extends CommandLiteralNode {
    private final CommandParser parser;
    private final ParserProperties properties;
    private final SuggestionTypes suggestionType;

    public CommandArgumentNode(byte flags, InByteBuffer buffer) {
        super(flags, buffer);
        ModIdentifier parserIdentifier = buffer.readIdentifier();
        this.parser = CommandParsers.INSTANCE.getParserInstance(parserIdentifier);
        if (this.parser == null) {
            Log.verbose("Unknown command parser: %s", parserIdentifier);
            this.properties = null;
        } else {
            this.properties = this.parser.readParserProperties(buffer);
        }
        if (BitByte.isBitMask(flags, 0x10)) {
            String fullIdentifier = buffer.readIdentifier().getFullIdentifier();
            this.suggestionType = switch (fullIdentifier) {
                case "minecraft:ask_server" -> CommandArgumentNode.SuggestionTypes.ASK_SERVER;
                case "minecraft:all_recipes" -> CommandArgumentNode.SuggestionTypes.ALL_RECIPES;
                case "minecraft:available_sounds" -> CommandArgumentNode.SuggestionTypes.AVAILABLE_SOUNDS;
                case "minecraft:summonable_entities" -> CommandArgumentNode.SuggestionTypes.SUMMONABLE_ENTITIES;
                case "minecraft:available_biomes" -> CommandArgumentNode.SuggestionTypes.AVAILABLE_BIOMES;
                default -> throw new IllegalStateException("Unexpected value: " + fullIdentifier);
            };
        } else {
            this.suggestionType = null;
        }
    }

    public CommandParser getParser() {
        return this.parser;
    }

    @Nullable
    public ParserProperties getProperties() {
        return this.properties;
    }

    public SuggestionTypes getSuggestionType() {
        return this.suggestionType;
    }

    @Override
    public void isSyntaxCorrect(Connection connection, CommandStringReader stringReader) throws CommandParseException {
        this.parser.isParsable(connection, this.properties, stringReader);
        super.isSyntaxCorrect(connection, stringReader);
    }

    public enum SuggestionTypes {
        ASK_SERVER,
        ALL_RECIPES,
        AVAILABLE_SOUNDS,
        SUMMONABLE_ENTITIES,
        AVAILABLE_BIOMES
    }
}
