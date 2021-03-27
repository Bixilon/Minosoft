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

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

public class PacketExplosion extends ClientboundPacket {
    private Vec3i position;
    private float radius;
    private byte[][] records;
    private float motionX;
    private float motionY;
    private float motionZ;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.position = new Vec3i(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.radius = buffer.readFloat();
        if (this.radius > 100.0F) {
            // maybe somebody tries to make bullshit?
            // Sorry, Maximilian Rosenmüller
            throw new IllegalArgumentException(String.format("Explosion to big %s > 100.0F", this.radius));
        }
        int recordCount = buffer.readInt();
        this.records = new byte[recordCount][3];
        for (int i = 0; i < recordCount; i++) {
            this.records[i] = buffer.readBytes(3);
        }

        this.motionX = buffer.readFloat();
        this.motionY = buffer.readFloat();
        this.motionZ = buffer.readFloat();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        // remove all blocks set by explosion
        for (byte[] record : getRecords()) {
            int x = getPosition().x + record[0];
            int y = getPosition().y + record[1];
            int z = getPosition().z + record[2];
            Vec3i blockPosition = new Vec3i(x, (short) y, z);
            connection.getPlayer().getWorld().setBlock(blockPosition, null);
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
