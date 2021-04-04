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

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_PRE4;

public class PlayerAbilitiesServerboundPacket implements PlayServerboundPacket {
    private final boolean flying;

    public PlayerAbilitiesServerboundPacket(boolean flying) {
        this.flying = flying;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        byte flags = 0;
        if (this.flying) {
            flags |= 0b10;
        }
        buffer.writeByte(flags);
        if (buffer.getVersionId() < V_1_16_PRE4) {
            // only fly matters, everything else ignored
            buffer.writeFloat(0.0F);
            buffer.writeFloat(0.0F);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending player abilities packet: (flying=%s)", this.flying));
    }
}
