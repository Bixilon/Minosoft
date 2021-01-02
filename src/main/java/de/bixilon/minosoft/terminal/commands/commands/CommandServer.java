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

import com.github.freva.asciitable.AsciiTable;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.mappings.versions.Versions;

import java.util.ArrayList;

public class CommandServer extends Command {

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("server",
                        new CommandLiteralNode("list", (stack) -> {
                            ArrayList<Object[]> tableData = new ArrayList<>();

                            for (var entry : Minosoft.getConfig().getServerList().entrySet()) {
                                tableData.add(new Object[]{entry.getKey(), entry.getValue().getAddress(), Versions.getVersionById(entry.getValue().getDesiredVersionId())});
                            }

                            print(AsciiTable.getTable(new String[]{"ID", "ADDRESS", "DESIRED VERSION"}, tableData.toArray(new Object[0][0])));
                        })));
        return parent;
    }
}
