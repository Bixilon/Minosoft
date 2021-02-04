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

package de.bixilon.minosoft.terminal.commands.commands;

import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.parser.StringParser;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;

public class CommandHelp extends Command {

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("help", (stack) -> {
                    print("============= help =============");
                    print("Commands in [Brackets] are connection specific!");
                    print("");
                    print("help -> Get this list");
                    print("connection -> Manage connections");
                    print("[disconnect]] -> Disconnect from current connection");
                    print("[chat] -> Write a command or chat message");
                    print("[tab] -> Manage tab");
                    print("[entity] -> See all entities");
                }, new CommandArgumentNode("command", StringParser.STRING_PARSER, new StringParserProperties(StringParserProperties.StringSettings.QUOTABLE_PHRASE, false), (stack) -> print("Coming soon :)"))));
        return parent;
    }
}
