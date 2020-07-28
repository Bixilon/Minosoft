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

import de.bixilon.minosoft.game.datatypes.world.ChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.ChunkUtil;

public class PacketUpdateLight implements ClientboundPacket {
    ChunkLocation location;

    @Override
    public boolean read(InByteBuffer buffer) {
        location = new ChunkLocation(buffer.readVarInt(), buffer.readVarInt());
        int skyLightMask = buffer.readVarInt();
        int blockLightMask = buffer.readVarInt();
        int emptyBlockLightMask = buffer.readVarInt();
        int emptySkyLightMask = buffer.readVarInt();
        ChunkUtil.readSkyLightPacket(buffer, skyLightMask, blockLightMask, emptyBlockLightMask, emptySkyLightMask);
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received sky light update (location=%s)", location));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
