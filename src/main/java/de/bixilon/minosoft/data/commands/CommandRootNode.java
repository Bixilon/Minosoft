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

import de.bixilon.minosoft.data.commands.parser.exception.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exception.UnknownCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exception.WrongArgumentCommandParseException;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class CommandRootNode extends CommandNode {
    public CommandRootNode(byte flags, InByteBuffer buffer) {
        super(flags, buffer);
    }

    @Override
    public void isSyntaxCorrect(Connection connection, ImprovedStringReader stringReader) throws CommandParseException {
        try {
            super.isSyntaxCorrect(connection, stringReader);
        } catch (WrongArgumentCommandParseException e) {
            if (e.getStartIndex() == 0) {
                // beginn of string
                throw new UnknownCommandParseException(stringReader, stringReader.getString().substring(0, e.getEndIndex()));
            }
            throw e;
        }
    }
}
