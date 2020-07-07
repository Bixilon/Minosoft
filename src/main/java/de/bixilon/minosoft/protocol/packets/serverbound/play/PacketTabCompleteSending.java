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
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketTabCompleteSending implements ServerboundPacket {
    final String text;
    final BlockPosition position;
    final boolean assumeCommand;

    public PacketTabCompleteSending(String text) {
        this.text = text;
        position = null;
        assumeCommand = false;
        log();
    }

    public PacketTabCompleteSending(String text, BlockPosition position) {
        this.text = text;
        this.position = position;
        assumeCommand = false;
        log();
    }


    public PacketTabCompleteSending(String text, boolean assumeCommand, BlockPosition position) {
        this.text = text;
        this.position = position;
        this.assumeCommand = assumeCommand;
        log();
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_TAB_COMPLETE));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeString(text);
                break;
            case VERSION_1_8:
                buffer.writeString(text);
                if (position == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writePosition(position);
                }
                break;
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                buffer.writeString(text);
                buffer.writeBoolean(assumeCommand);
                if (position == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writePosition(position);
                }
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending tab complete for message=\"%s\"", text.replace("\"", "\\\"")));
    }
}
