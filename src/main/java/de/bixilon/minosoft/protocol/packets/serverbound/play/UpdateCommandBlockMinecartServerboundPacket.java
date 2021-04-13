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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class UpdateCommandBlockMinecartServerboundPacket implements PlayServerboundPacket {
    private final int entityId;
    private final String command;
    private final boolean trackOutput;

    public UpdateCommandBlockMinecartServerboundPacket(int entityId, String command, boolean trackOutput) {
        this.entityId = entityId;
        this.command = command;
        this.trackOutput = trackOutput;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeString(this.command);
        buffer.writeBoolean(this.trackOutput);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending update minecart command block packet (entityId=%d, command=\"%s\", trackOutput=%s)", this.entityId, this.command, this.trackOutput));
    }
}
