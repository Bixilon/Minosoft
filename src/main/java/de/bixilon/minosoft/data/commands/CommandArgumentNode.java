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

package de.bixilon.minosoft.data.commands;

import de.bixilon.minosoft.data.commands.parser.CommandParser;
import de.bixilon.minosoft.data.commands.parser.CommandParsers;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.terminal.commands.CommandStack;
import de.bixilon.minosoft.terminal.commands.exceptions.CLIException;
import de.bixilon.minosoft.terminal.commands.executors.CommandConnectionExecutor;
import de.bixilon.minosoft.terminal.commands.executors.CommandExecutor;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;
import de.bixilon.minosoft.util.logging.LogMessageType;

import javax.annotation.Nullable;

public class CommandArgumentNode extends CommandLiteralNode {
    private final CommandParser parser;
    private ParserProperties properties;
    private SuggestionTypes suggestionType;

    public CommandArgumentNode(int flags, InByteBuffer buffer) {
        super(flags, buffer);
        ResourceLocation parserResourceLocation = buffer.readResourceLocation();
        this.parser = CommandParsers.INSTANCE.getParserInstance(parserResourceLocation);
        if (this.parser == null) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE, () -> "Unknown command parser:" + parserResourceLocation);
        } else {
            this.properties = this.parser.readParserProperties(buffer);
        }
        if (BitByte.isBitMask(flags, 0x10)) {
            String resourceLocation = buffer.readResourceLocation().getFull();
            switch (resourceLocation) {
                case "minecraft:ask_server":
                    this.suggestionType = CommandArgumentNode.SuggestionTypes.ASK_SERVER;
                    break;
                case "minecraft:all_recipes":
                    this.suggestionType = CommandArgumentNode.SuggestionTypes.ALL_RECIPES;
                    break;
                case "minecraft:available_sounds":
                    this.suggestionType = CommandArgumentNode.SuggestionTypes.AVAILABLE_SOUNDS;
                    break;
                case "minecraft:summonable_entities":
                    this.suggestionType = CommandArgumentNode.SuggestionTypes.SUMMONABLE_ENTITIES;
                    break;
                case "minecraft:available_biomes":
                    this.suggestionType = CommandArgumentNode.SuggestionTypes.AVAILABLE_BIOMES;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value: " + resourceLocation);
            }
        }
    }

    public CommandArgumentNode(String name, CommandParser parser, CommandExecutor executor, CommandNode... children) {
        super(name, executor, children);
        this.parser = parser;
    }

    public CommandArgumentNode(String name, CommandParser parser, ParserProperties properties, CommandExecutor executor, CommandNode... children) {
        super(name, executor, children);
        this.parser = parser;
        this.properties = properties;
    }

    public CommandArgumentNode(String name, CommandParser parser, CommandConnectionExecutor executor, CommandNode... children) {
        super(name, executor, children);
        this.parser = parser;
    }

    public CommandArgumentNode(String name, CommandParser parser, ParserProperties properties, CommandConnectionExecutor executor, CommandNode... children) {
        super(name, executor, children);
        this.parser = parser;
        this.properties = properties;
    }

    public CommandArgumentNode(String name, CommandParser parser, CommandNode... children) {
        super(name, children);
        this.parser = parser;
    }

    public CommandArgumentNode(String name, CommandParser parser, ParserProperties properties, CommandNode... children) {
        super(name, children);
        this.parser = parser;
        this.properties = properties;
    }

    public CommandParser getParser() {
        return this.parser;
    }

    @Nullable
    public ParserProperties getProperties() {
        return this.properties;
    }

    @Nullable
    public SuggestionTypes getSuggestionType() {
        return this.suggestionType;
    }

    @Override
    public CommandStack parse(PlayConnection connection, CommandStringReader stringReader, CommandStack stack, boolean execute) throws CommandParseException, CLIException {
        stack.addArgument(this.parser.parse(connection, this.properties, stringReader));
        return super.parse(connection, stringReader, stack, execute);
    }

    public enum SuggestionTypes {
        ASK_SERVER,
        ALL_RECIPES,
        AVAILABLE_SOUNDS,
        SUMMONABLE_ENTITIES,
        AVAILABLE_BIOMES
    }
}
