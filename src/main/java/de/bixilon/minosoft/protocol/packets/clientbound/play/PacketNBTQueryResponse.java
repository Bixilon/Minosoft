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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

public class PacketNBTQueryResponse extends PlayClientboundPacket {
    private final int transactionId;
    private final CompoundTag tag;

    public PacketNBTQueryResponse(PlayInByteBuffer buffer) {
        this.transactionId = buffer.readVarInt();
        this.tag = (CompoundTag) buffer.readNBT();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received nbt response (transactionId=%d, nbt=%s)", this.transactionId, this.tag.toString()));
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
