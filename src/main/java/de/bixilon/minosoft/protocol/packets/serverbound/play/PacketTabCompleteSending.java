/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketTabCompleteSending implements ServerboundPacket {
    final String text;
    final BlockPosition position;
    final boolean assumeCommand;

    public PacketTabCompleteSending(String text) {
        this.text = text;
        position = null;
        assumeCommand = false;
    }

    public PacketTabCompleteSending(String text, BlockPosition position) {
        this.text = text;
        this.position = position;
        assumeCommand = false;
    }

    public PacketTabCompleteSending(String text, boolean assumeCommand, BlockPosition position) {
        this.text = text;
        this.position = position;
        this.assumeCommand = assumeCommand;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_TAB_COMPLETE);
        buffer.writeString(text);
        if (buffer.getProtocolId() >= 59) {
            buffer.writeBoolean(assumeCommand);
        }
        if (buffer.getProtocolId() >= 37) {
            if (position == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                buffer.writePosition(position);
            }
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending tab complete for message=\"%s\"", text.replace("\"", "\\\"")));
    }
}
