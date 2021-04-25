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

import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockBreakC2SP;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

public class PacketAcknowledgePlayerDigging extends PlayS2CPacket {
    private final Vec3i blockPosition;
    private final BlockState block;
    private final BlockBreakC2SP.BreakType status;
    private final boolean successful;

    public PacketAcknowledgePlayerDigging(PlayInByteBuffer buffer) {
        this.blockPosition = buffer.readBlockPosition();
        this.block = buffer.getConnection().getMapping().getBlockState(buffer.readVarInt());
        this.status = BlockBreakC2SP.BreakType.Companion.getVALUES()[buffer.readVarInt()];
        this.successful = buffer.readBoolean();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received acknowledge digging packet (position=%s, block=%s, status=%s, successful=%s)", this.blockPosition, this.block, this.status, this.successful));
    }

    public Vec3i getBlockPosition() {
        return this.blockPosition;
    }

    public BlockState getBlock() {
        return this.block;
    }

    public BlockBreakC2SP.BreakType getStatus() {
        return this.status;
    }

    public boolean isSuccessful() {
        return this.successful;
    }
}
