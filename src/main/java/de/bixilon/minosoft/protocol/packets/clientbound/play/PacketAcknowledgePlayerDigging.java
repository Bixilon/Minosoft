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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerDigging;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketAcknowledgePlayerDigging extends ClientboundPacket {
    BlockPosition position;
    BlockState block;
    PacketPlayerDigging.DiggingStatus status;
    boolean successful;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.position = buffer.readPosition();
        this.block = buffer.getConnection().getMapping().getBlockState(buffer.readVarInt());
        this.status = PacketPlayerDigging.DiggingStatus.byId(buffer.readVarInt());
        this.successful = buffer.readBoolean();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received acknowledge digging packet (position=%s, block=%s, status=%s, successful=%s)", this.position, this.block, this.status, this.successful));
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockState getBlock() {
        return this.block;
    }

    public PacketPlayerDigging.DiggingStatus getStatus() {
        return this.status;
    }

    public boolean isSuccessful() {
        return this.successful;
    }
}
