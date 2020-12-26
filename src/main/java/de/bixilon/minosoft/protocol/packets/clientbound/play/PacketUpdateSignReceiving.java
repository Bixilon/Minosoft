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

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.StringTag;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketUpdateSignReceiving extends ClientboundPacket {
    final ChatComponent[] lines = new ChatComponent[ProtocolDefinition.SIGN_LINES];
    BlockPosition position;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W04A) {
            this.position = buffer.readBlockPositionShort();
        } else {
            this.position = buffer.readPosition();
        }
        for (byte i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
            this.lines[i] = buffer.readChatComponent();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        CompoundTag nbt = new CompoundTag();
        nbt.writeBlockPosition(getPosition());
        nbt.writeTag("id", new StringTag("minecraft:sign"));
        for (int i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
            nbt.writeTag(String.format("Text%d", (i + 1)), new StringTag(getLines()[i].getLegacyText()));
        }
        // ToDo: handle sign updates
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Sign data received at: %s", this.position));
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public ChatComponent[] getLines() {
        return this.lines;
    }
}
