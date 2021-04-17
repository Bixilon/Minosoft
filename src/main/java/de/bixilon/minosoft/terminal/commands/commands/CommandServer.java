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
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.parser.IntegerParser;
import de.bixilon.minosoft.data.commands.parser.StringParser;
import de.bixilon.minosoft.data.commands.parser.minosoft.VersionParser;
import de.bixilon.minosoft.data.commands.parser.properties.IntegerParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.StringParserProperties;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.gui.main.ServerListCell;
import javafx.application.Platform;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CommandServer extends Command {

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("server",
                        new CommandLiteralNode("list", (stack) -> {
                            ArrayList<Object[]> tableData = new ArrayList<>();

                            for (var entry : Minosoft.getConfig().getConfig().getServer().getEntries().entrySet()) {
                                tableData.add(new Object[]{entry.getKey(), entry.getValue().getName(), entry.getValue().getAddress(), Versions.getVersionById(entry.getValue().getDesiredVersionId())});
                            }

                            print(AsciiTable.getTable(new String[]{"ID", "NAME", "ADDRESS", "DESIRED VERSION"}, tableData.toArray(new Object[0][0])));
                        }),
                        new CommandLiteralNode("add",
                                new CommandArgumentNode("name", StringParser.STRING_PARSER, new StringParserProperties(StringParserProperties.StringSettings.QUOTABLE_PHRASE, false),
                                        new CommandArgumentNode("address", StringParser.STRING_PARSER, new StringParserProperties(StringParserProperties.StringSettings.QUOTABLE_PHRASE, false), (stack) -> addServer(stack.getNonLiteralArgument(0), stack.getNonLiteralArgument(1), null),
                                                new CommandArgumentNode("version", VersionParser.VERSION_PARSER, ((stack) -> addServer(stack.getNonLiteralArgument(0), stack.getNonLiteralArgument(1), stack.getNonLiteralArgument(2))))))),

                        new CommandLiteralNode("delete", new CommandArgumentNode("id", IntegerParser.INTEGER_PARSER, new IntegerParserProperties(0, Integer.MAX_VALUE), (stack -> {
                            Server server = Minosoft.getConfig().getConfig().getServer().getEntries().get(stack.getInt(0));
                            if (server == null) {
                                printError("Server not found!");
                                return;
                            }
                            Minosoft.getConfig().getConfig().getServer().getEntries().remove(server.getId());
                            ServerListCell.SERVER_LIST_VIEW.getItems().remove(server);
                        })))
                ));
        return parent;
    }

    private void addServer(String name, String address, @Nullable Version version) {
        if (version == null) {
            version = Versions.AUTOMATIC_VERSION;
        }
        Server server = new Server(ChatComponent.Companion.valueOf(name), address, version);

        server.saveToConfig();
        print("Added server %s (address=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersionId());
        Platform.runLater(() -> ServerListCell.SERVER_LIST_VIEW.getItems().add(server));
    }
}
