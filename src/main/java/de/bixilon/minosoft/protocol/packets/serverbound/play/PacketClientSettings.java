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
import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketClientSettings implements ServerboundPacket {

    public final Locale locale;
    public final byte renderDistance;

    public final Hand mainHand;

    public PacketClientSettings(Locale locale, int renderDistance) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
        this.mainHand = Hand.RIGHT; // unused; >= 1.9
        log();
    }

    public PacketClientSettings(Locale locale, int renderDistance, Hand mainHand) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
        this.mainHand = mainHand;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_CLIENT_SETTINGS));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeString(locale.getName()); // locale
                buffer.writeByte(renderDistance); // render Distance
                buffer.writeByte((byte) 0x00); // chat settings (nobody uses them)
                buffer.writeBoolean(true); // chat colors
                buffer.writeByte((byte) Difficulty.NORMAL.getId()); // difficulty
                buffer.writeBoolean(true); // cape
                break;
            case VERSION_1_8:
                buffer.writeString(locale.getName()); // locale
                buffer.writeByte(renderDistance); // render Distance
                buffer.writeByte((byte) 0x00); // chat settings (nobody uses them)
                buffer.writeBoolean(true); // chat colors
                buffer.writeByte((byte) 0b01111111); // skin parts
                break;
            case VERSION_1_9_4:
                buffer.writeString(locale.getName()); // locale
                buffer.writeByte(renderDistance); // render Distance
                buffer.writeVarInt(0x00); // chat settings (nobody uses them)
                buffer.writeBoolean(true); // chat colors
                buffer.writeByte((byte) 0b01111111); // skin parts
                buffer.writeVarInt(mainHand.getId());
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending settings (locale=%s, renderDistance=%d)", locale.getName(), renderDistance));
    }
}
