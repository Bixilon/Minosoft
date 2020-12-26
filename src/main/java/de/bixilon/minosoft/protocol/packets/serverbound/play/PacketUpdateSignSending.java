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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketUpdateSignSending implements ServerboundPacket {
    final BlockPosition position;
    final ChatComponent[] lines;

    public PacketUpdateSignSending(BlockPosition position, ChatComponent[] lines) {
        this.position = position;
        this.lines = lines;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_UPDATE_SIGN);
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeBlockPositionByte(this.position);
        } else {
            buffer.writePosition(this.position);
        }
        if (buffer.getVersionId() < V_14W25A || buffer.getVersionId() >= V_15W35A) {
            for (int i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
                buffer.writeString(this.lines[i].getMessage());
            }
        } else {
            for (int i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
                buffer.writeChatComponent(this.lines[i]);
            }
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending Sign Update: %s", this.position));
    }
}
