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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketOpenSignEditor implements ClientboundPacket {
    BlockPosition position;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                position = buffer.readBlockPositionInteger();
                break;
            case VERSION_1_8:
                position = buffer.readPosition();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Opening sign editor: %s", position.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public BlockPosition getPosition() {
        return position;
    }
}
