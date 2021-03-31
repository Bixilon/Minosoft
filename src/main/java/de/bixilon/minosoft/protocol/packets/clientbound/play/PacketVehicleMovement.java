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

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

public class PacketVehicleMovement extends ClientboundPacket {
    private final Vec3 position;
    private final float yaw;
    private final float pitch;

    public PacketVehicleMovement(InByteBuffer buffer) {
        this.position = buffer.readEntityPosition();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received vehicle movement (position=%s, yaw=%s, pitch=%s)", this.position, this.yaw, this.pitch));
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
