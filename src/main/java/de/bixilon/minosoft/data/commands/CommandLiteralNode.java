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

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.terminal.commands.executors.CommandConnectionExecutor;
import de.bixilon.minosoft.terminal.commands.executors.CommandExecutor;

public class CommandLiteralNode extends CommandNode {
    private final String name;

    public CommandLiteralNode(byte flags, InByteBuffer buffer) {
        super(flags, buffer);
        this.name = buffer.readString();
    }


    public CommandLiteralNode(String name, CommandNode... children) {
        super(children);
        this.name = name;
    }

    public CommandLiteralNode(String name, CommandExecutor executor, CommandNode... children) {
        super(executor, children);
        this.name = name;
    }

    public CommandLiteralNode(String name, CommandConnectionExecutor executor, CommandNode... children) {
        super(executor, children);
        this.name = name;
    }


    public String getName() {
        return this.name;
    }

}
