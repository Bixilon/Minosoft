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

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_10_PRE1;

public class ResourcePackStatusServerboundPacket implements PlayServerboundPacket {
    private final String hash;
    private final ResourcePackStates status;

    public ResourcePackStatusServerboundPacket(String hash, ResourcePackStates status) {
        this.hash = hash;
        this.status = status;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        if (buffer.getVersionId() < V_1_10_PRE1) {
            buffer.writeString(this.hash);
        }
        buffer.writeVarInt(this.status.ordinal());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending resource pack status (status=%s, hash=%s)", this.status, this.hash));
    }

    public enum ResourcePackStates {
        SUCCESSFULLY,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED
    }
}
