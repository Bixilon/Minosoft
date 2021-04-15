/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec2.Vec2i;

public class PacketUnloadChunk extends PlayS2CPacket {
    private final Vec2i chunkPosition;

    public PacketUnloadChunk(PlayInByteBuffer buffer) {
        this.chunkPosition = buffer.readChunkPosition();
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.getWorld().unloadChunk(getChunkPosition());
        connection.getRenderer().getRenderWindow().getWorldRenderer().unloadChunk(this.chunkPosition);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received unload chunk packet (chunkPosition=%s)", this.chunkPosition));
    }

    public Vec2i getChunkPosition() {
        return this.chunkPosition;
    }
}
