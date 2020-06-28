/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketExplosion implements ClientboundPacket {
    Location location;
    float radius;
    byte[][] records;
    float motionX;
    float motionY;
    float motionZ;

    @Override
    public void read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
                location = new Location(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                radius = buffer.readFloat();
                if (radius > 100.0F) {
                    // maybe somebody tries to crash me?
                    // Sorry, Maximilian Rosenmüller
                    throw new IllegalArgumentException(String.format("Explosion to big %s > 100.0F", radius));
                }
                int recordCount = buffer.readInteger();
                records = new byte[recordCount][3];
                for (int i = 0; i < recordCount; i++) {
                    records[i] = buffer.readBytes(3);
                }

                motionX = buffer.readFloat();
                motionY = buffer.readFloat();
                motionZ = buffer.readFloat();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Explosion packet received at %s (recordCount=%d, radius=%s)", location.toString(), records.length, radius));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Location getLocation() {
        return location;
    }

    public float getMotionX() {
        return motionX;
    }

    public float getMotionY() {
        return motionY;
    }

    public float getMotionZ() {
        return motionZ;
    }

    public byte[][] getRecords() {
        return records;
    }

    public float getRadius() {
        return radius;
    }
}
