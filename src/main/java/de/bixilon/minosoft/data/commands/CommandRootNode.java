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

import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.FloatingDataCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.WrongArgumentCommandParseException;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.terminal.commands.CommandStack;
import de.bixilon.minosoft.terminal.commands.exceptions.CLIException;

public class CommandRootNode extends CommandNode {
    public CommandRootNode(byte flags, InByteBuffer buffer) {
        super(flags, buffer);
    }

    public CommandRootNode(CommandNode... children) {
        super(children);
    }

    @Override
    public CommandStack parse(PlayConnection connection, CommandStringReader stringReader, CommandStack stack, boolean execute) throws CommandParseException, CLIException {
        try {
            stack = super.parse(connection, stringReader, stack, execute);
            if (stringReader.getRemainingLength() > 0) {
                throw new FloatingDataCommandParseException(stringReader, stringReader.readRemaining());
            }
            return stack;
        } catch (WrongArgumentCommandParseException e) {
            if (e.getStartIndex() == 0) {
                // beginn of string
                throw new UnknownCommandParseException(stringReader, stringReader.getString().substring(0, e.getEndIndex()));
            }
            throw e;
        }
    }
}
