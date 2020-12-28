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

import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W22A;

public class PacketGenerateStructure implements ServerboundPacket {
    private final BlockPosition position;
    private final int levels;
    private final boolean keepJigsaw;

    public PacketGenerateStructure(BlockPosition position, int levels, boolean keepJigsaw) {
        this.position = position;
        this.levels = levels;
        this.keepJigsaw = keepJigsaw;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_GENERATE_STRUCTURE);
        buffer.writePosition(this.position);
        buffer.writeVarInt(this.levels);
        if (buffer.getVersionId() <= V_20W22A) {
            buffer.writeBoolean(this.keepJigsaw);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending generate structure packet (position=%s, levels=%d, keepJigsaw=%s)", this.position, this.levels, this.keepJigsaw));
    }
}
