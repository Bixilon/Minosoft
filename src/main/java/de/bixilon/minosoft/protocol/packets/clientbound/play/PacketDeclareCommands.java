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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class PacketDeclareCommands extends ClientboundPacket {
    private CommandRootNode rootNode;

    @Override
    public boolean read(InByteBuffer buffer) throws Exception {
        CommandNode[] nodes = buffer.readCommandNodesArray();
        this.rootNode = (CommandRootNode) nodes[buffer.readVarInt()];
        return true;
    }

    public CommandRootNode getRootNode() {
        return this.rootNode;
    }

    @Override
    public void handle(Connection connection) {
        connection.setCommandRootNode(getRootNode());
        // ToDo: Remove these dummy commands
        String[] commands = {
                "setblock ~3 ~3 3 minecraft:anvil",
                "setblock ~ ~3 ^3 minecraft:anvil",
                "setblock ^ ^3 ^3 minecraft:anvil",
        };
        for (String command : commands) {
            try {
                getRootNode().isSyntaxCorrect(connection, command);
                Log.game("Command \"%s\" is valid", command);
            } catch (CommandParseException e) {
                Log.game("Command \"%s\" is invalid, %s: %s", command, e.getClass().getSimpleName(), e.getErrorMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void log() {
        Log.protocol("Received declare commands packets");
    }
}
