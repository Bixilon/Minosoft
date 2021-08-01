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

package de.bixilon.minosoft.terminal.commands.commands;

import com.github.freva.asciitable.AsciiTable;
import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.parser.IntegerParser;
import de.bixilon.minosoft.data.commands.parser.properties.IntegerParserProperties;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.terminal.CLI;
import de.bixilon.minosoft.terminal.commands.exceptions.ConnectionNotFoundCommandParseException;

import java.util.ArrayList;

public class CommandConnection extends Command {

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("connection",
                        new CommandLiteralNode("list", (stack) -> {
                            ArrayList<Object[]> tableData = new ArrayList<>();

                            // ToDo
                            // for (var entry : Minosoft.CONNECTIONS.entrySet()) {
                            //     tableData.add(new Object[]{entry.getKey(), entry.getValue().getAddress(), entry.getValue().getAccount()});
                            // }

                            print(AsciiTable.getTable(new String[]{"ID", "ADDRESS", "ACCOUNT"}, tableData.toArray(new Object[0][0])));
                        }),
                        new CommandLiteralNode("select", new CommandArgumentNode("connectionId", IntegerParser.INTEGER_PARSER, new IntegerParserProperties(0), (stack) -> {
                            int connectionId = stack.getInt(0);
                            //ToDo PlayConnection connection = Minosoft.CONNECTIONS.get(connectionId);
                            PlayConnection connection = null;
                            if (connection == null) {
                                throw new ConnectionNotFoundCommandParseException(stack, connectionId);
                            }
                            CLI.setCurrentConnection(connection);
                            print("Current connection changed %s", connection);
                        })),
                        new CommandLiteralNode("current", (stack) -> {
                            PlayConnection connection = CLI.getCurrentConnection();
                            if (connection == null) {
                                print("No connection selected");
                                return;
                            }
                            print("Current connection: %s", connection);
                        })));
        return parent;
    }
}
