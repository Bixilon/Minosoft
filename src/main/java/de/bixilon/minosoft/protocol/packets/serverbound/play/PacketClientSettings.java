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

import de.bixilon.minosoft.game.datatypes.Difficulty;
import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketClientSettings implements ServerboundPacket {

    public final String locale;
    public final byte renderDistance;

    public Hand mainHand;

    public PacketClientSettings(String locale, int renderDistance) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
    }

    public PacketClientSettings(String locale, int renderDistance, Hand mainHand) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
        this.mainHand = mainHand;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_CLIENT_SETTINGS);
        buffer.writeString(locale); // locale
        buffer.writeByte(renderDistance); // render Distance
        buffer.writeByte((byte) 0x00); // chat settings (nobody uses them)
        buffer.writeBoolean(true); // chat colors
        if (buffer.getProtocolId() < 6) {
            buffer.writeByte((byte) Difficulty.NORMAL.getId()); // difficulty
            buffer.writeBoolean(true); // cape
        } else {
            buffer.writeByte((byte) 0b01111111); // ToDo: skin parts
        }
        if (buffer.getProtocolId() >= 49) {
            buffer.writeVarInt(mainHand.getId());
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending settings (locale=%s, renderDistance=%d)", locale, renderDistance));
    }
}
