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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;
import org.jetbrains.annotations.NotNull;

public class PacketExplosion extends PlayS2CPacket {
    private final Vec3i position;
    private final float radius;
    private final byte[][] records;
    private final float motionX;
    private final float motionY;
    private final float motionZ;

    public PacketExplosion(PlayInByteBuffer buffer) {
        this.position = new Vec3i(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.radius = buffer.readFloat();
        int recordCount = buffer.readInt();
        this.records = new byte[recordCount][3];
        for (int i = 0; i < recordCount; i++) {
            this.records[i] = buffer.readBytes(3);
        }

        this.motionX = buffer.readFloat();
        this.motionY = buffer.readFloat();
        this.motionZ = buffer.readFloat();
    }

    @Override
    public void check(@NotNull PlayConnection connection) {
        if (this.radius > 100.0F) {
            // maybe somebody tries to make bullshit?
            // Sorry, Maximilian Rosenmüller
            throw new IllegalArgumentException(String.format("Explosion to big %s > 100.0F", this.radius));
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        // remove all blocks set by explosion
        for (byte[] record : getRecords()) {
            int x = getPosition().getX() + record[0];
            int y = getPosition().getY() + record[1];
            int z = getPosition().getZ() + record[2];
            Vec3i blockPosition = new Vec3i(x, (short) y, z);
            connection.getWorld().setBlock(blockPosition, null);
        }
        // ToDo: motion support
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Explosion packet received at %s (recordCount=%d, radius=%s)", this.position, this.records.length, this.radius));
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public float getMotionX() {
        return this.motionX;
    }

    public float getMotionY() {
        return this.motionY;
    }

    public float getMotionZ() {
        return this.motionZ;
    }

    public byte[][] getRecords() {
        return this.records;
    }

    public float getRadius() {
        return this.radius;
    }
}
