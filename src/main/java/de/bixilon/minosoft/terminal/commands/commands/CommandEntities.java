/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.minosoft.data.entities.entities.Entity;

import java.util.ArrayList;

public class CommandEntities extends Command {

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("entity",
                        new CommandLiteralNode("list", (connection, stack) -> {
                            ArrayList<Object[]> tableData = new ArrayList<>();

                            for (Entity entry : connection.getWorld().getEntities()) {
                                tableData.add(new Object[]{connection.getWorld().getEntities().getId(entry), connection.getWorld().getEntities().getUUID(entry), entry.getType().toString(), entry.getEquipment(), entry.getPosition(), entry.getRotation()});
                            }

                            print(AsciiTable.getTable(new String[]{"ID", "UUID", "TYPE", "EQUIPMENT", "LOCATION", "ROTATION"}, tableData.toArray(new Object[0][0])));
                        }),
                        new CommandLiteralNode("info", new CommandArgumentNode("entityId", IntegerParser.INTEGER_PARSER, new IntegerParserProperties(0, Integer.MAX_VALUE), (connection, stack) -> {
                            // ToDo: entity uuids

                            Entity entity = connection.getWorld().getEntities().get(stack.getInt(0));
                            if (entity == null) {
                                printError("Entity %d not found!", stack.getInt(0));
                                return;
                            }
                            ArrayList<Object[]> tableData = new ArrayList<>();

                            tableData.add(new Object[]{"Entity id", connection.getWorld().getEntities().getId(entity)});
                            tableData.add(new Object[]{"UUID", connection.getWorld().getEntities().getUUID(entity)});
                            tableData.add(new Object[]{"Type", entity.getType()});
                            tableData.add(new Object[]{"Class", entity.getClass().getName()});
                            tableData.add(new Object[]{"Location", entity.getPosition()});
                            tableData.add(new Object[]{"Rotation", entity.getRotation()});
                            tableData.add(new Object[]{"Equipment", entity.getEquipment()});
                            tableData.add(new Object[]{"Effects", entity.getActiveStatusEffects()});
                            tableData.add(new Object[]{"Attached to", entity.get_attachedEntity() == -1 ? "" : entity.get_attachedEntity()});


                            print(AsciiTable.getTable(new String[]{"PROPERTY", "VALUE"}, tableData.toArray(new Object[0][0])));
                        }))));
        return parent;
    }
}
