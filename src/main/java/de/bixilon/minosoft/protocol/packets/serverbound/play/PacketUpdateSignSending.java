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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketUpdateSignSending implements ServerboundPacket {
    final BlockPosition position;
    final TextComponent[] lines;

    public PacketUpdateSignSending(BlockPosition position, TextComponent[] lines) {
        this.position = position;
        this.lines = lines;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_UPDATE_SIGN));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeBlockPositionByte(position);
                for (int i = 0; i < 4; i++) {
                    buffer.writeString(lines[i].getRawMessage());
                }
                break;
            case VERSION_1_8:
                buffer.writePosition(position);
                for (int i = 0; i < 4; i++) {
                    buffer.writeTextComponent(lines[i]);
                }
                break;
            case VERSION_1_9_4:
                buffer.writePosition(position);
                for (int i = 0; i < 4; i++) {
                    buffer.writeString(lines[i].getRawMessage());
                }
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending Sign Update: %s", position.toString()));
    }
}
