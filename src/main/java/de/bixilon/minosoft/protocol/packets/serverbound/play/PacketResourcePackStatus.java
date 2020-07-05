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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketResourcePackStatus implements ServerboundPacket {
    final String hash;
    final ResourcePackStatus status;

    public PacketResourcePackStatus(String hash, ResourcePackStatus status) {
        this.hash = hash;
        this.status = status;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_RESOURCE_PACK_STATUS));
        switch (version) {
            case VERSION_1_8:
            case VERSION_1_9_4:
                buffer.writeString(hash);
                buffer.writeVarInt(status.getId());
                break;
            case VERSION_1_10:
            case VERSION_1_11_2:
                buffer.writeVarInt(status.getId());
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending resource pack status (status=%s, hash=%s)", status.name(), hash));
    }

    public enum ResourcePackStatus {
        SUCCESSFULLY(0),
        DECLINED(1),
        FAILED_DOWNLOAD(2),
        ACCEPTED(3);

        final int id;

        ResourcePackStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
