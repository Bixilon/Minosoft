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
import de.bixilon.minosoft.game.datatypes.Locale;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketClientSettings implements ServerboundPacket {

    public final Locale locale;
    public final byte renderDistance;

    public PacketClientSettings(Locale locale, int renderDistance) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.PLAY_CLIENT_SETTINGS));
        switch (v) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                buffer.writeString(locale.getName()); // locale
                buffer.writeByte(renderDistance); // render Distance
                buffer.writeByte((byte) 0x00); // chat settings (nobody uses them)
                buffer.writeBoolean(true); // chat colors
                //skip this if not 1.7.10
                if (v == ProtocolVersion.VERSION_1_7_10) {
                    buffer.writeByte((byte) Difficulty.NORMAL.getId()); // difficulty
                }
                buffer.writeBoolean(true); // cape
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending settings (locale=%s, renderDistance=%d)", locale.getName(), renderDistance));
    }
}
