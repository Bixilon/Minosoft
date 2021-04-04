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

package de.bixilon.minosoft.protocol.packets.serverbound.status;

import de.bixilon.minosoft.protocol.packets.serverbound.AllServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionPing;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import org.jetbrains.annotations.NotNull;

public class StatusPingServerboundPacket implements AllServerboundPacket {
    private final long id;

    public StatusPingServerboundPacket(long id) {
        this.id = id;
    }

    public StatusPingServerboundPacket(int id) {
        this.id = id;
    }

    public StatusPingServerboundPacket(ConnectionPing ping) {
        this.id = ping.getPingId();
    }

    @Override
    public void write(@NotNull OutByteBuffer buffer) {
        buffer.writeLong(this.id);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending ping packet (%s)", this.id));
    }
}
