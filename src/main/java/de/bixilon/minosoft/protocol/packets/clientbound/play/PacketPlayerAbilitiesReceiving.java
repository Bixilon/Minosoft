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
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B;

public class PacketPlayerAbilitiesReceiving extends PlayClientboundPacket {
    private final boolean creative; // is this needed? receiving the gamemode in change Game state
    private final boolean flying;
    private final boolean canFly;
    private final boolean godMode;
    private final float flyingSpeed;
    private final float walkingSpeed;

    public PacketPlayerAbilitiesReceiving(PlayInByteBuffer buffer) {
        byte flags = buffer.readByte();
        if (buffer.getVersionId() < V_14W03B) { // ToDo
            this.creative = BitByte.isBitSet(flags, 0);
            this.flying = BitByte.isBitSet(flags, 1);
            this.canFly = BitByte.isBitSet(flags, 2);
            this.godMode = BitByte.isBitSet(flags, 3);
        } else {
            this.godMode = BitByte.isBitSet(flags, 0);
            this.flying = BitByte.isBitSet(flags, 1);
            this.canFly = BitByte.isBitSet(flags, 2);
            this.creative = BitByte.isBitSet(flags, 3);
        }
        this.flyingSpeed = buffer.readFloat();
        this.walkingSpeed = buffer.readFloat();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received player abilities packet: (creative=%s, flying=%s, canFly=%s, godMode=%s, flyingSpeed=%s, walkingSpeed=%s)", this.creative, this.flying, this.canFly, this.godMode, this.flyingSpeed, this.walkingSpeed));
    }

    public boolean canFly() {
        return this.canFly;
    }

    public boolean isCreative() {
        return this.creative;
    }

    public boolean isGodMode() {
        return this.godMode;
    }

    public boolean isFlying() {
        return this.flying;
    }
}
