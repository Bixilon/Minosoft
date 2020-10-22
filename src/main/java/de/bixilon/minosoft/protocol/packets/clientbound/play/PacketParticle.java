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

import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketParticle implements ClientboundPacket {
    Particle particleType;
    ParticleData particleData;
    boolean longDistance = false;
    Location location;
    float offsetX;
    float offsetY;
    float offsetZ;
    float particleDataFloat;
    int count;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 569) {
            if (buffer.getVersionId() < 17) {
                particleType = buffer.getConnection().getMapping().getParticleByIdentifier(buffer.readString());
            } else {
                particleType = buffer.getConnection().getMapping().getParticleById(buffer.readInt());
            }
            if (buffer.getVersionId() >= 29) {
                longDistance = buffer.readBoolean();
            }
            location = buffer.readSmallLocation();

            // offset
            offsetX = buffer.readFloat();
            offsetY = buffer.readFloat();
            offsetZ = buffer.readFloat();

            particleDataFloat = buffer.readFloat();
            count = buffer.readInt();
            particleData = buffer.readParticleData(particleType);
            return true;
        }
        particleType = buffer.getConnection().getMapping().getParticleById(buffer.readInt());
        longDistance = buffer.readBoolean();
        location = buffer.readLocation();

        // offset
        offsetX = buffer.readFloat();
        offsetY = buffer.readFloat();
        offsetZ = buffer.readFloat();

        particleDataFloat = buffer.readFloat();
        count = buffer.readInt();
        particleData = buffer.readParticleData(particleType);
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received particle spawn packet (location=%s, offsetX=%s, offsetY=%s, offsetZ=%s, particleType=%s, dataFloat=%s, count=%d, particleData=%s)", location, offsetX, offsetY, offsetZ, particleType, particleDataFloat, count, particleData));
    }

    public Location getLocation() {
        return location;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public int getCount() {
        return count;
    }

    public Particle getParticleType() {
        return particleType;
    }

    public ParticleData getParticleData() {
        return particleData;
    }

    public float getParticleDataFloat() {
        return particleDataFloat;
    }

    public boolean isLongDistance() {
        return longDistance;
    }
}
