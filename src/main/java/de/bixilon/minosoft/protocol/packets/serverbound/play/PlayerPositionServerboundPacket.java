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
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W06B;

public class PlayerPositionServerboundPacket implements PlayServerboundPacket {
    private final Vec3 position;
    private final boolean onGround;

    public PlayerPositionServerboundPacket(Vec3 position, boolean onGround) {
        this.position = position;
        this.onGround = onGround;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeDouble(this.position.getX());
        buffer.writeDouble(this.position.getY());
        if (buffer.getVersionId() < V_14W06B) {
            buffer.writeDouble(0.0); // ToDo
        }
        buffer.writeDouble(this.position.getZ());
        buffer.writeBoolean(this.onGround);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending player position (position=%s)", this.position));
    }
}
