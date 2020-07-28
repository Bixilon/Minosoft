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

import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particles;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.ParticleData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketParticle implements ClientboundPacket {
    Particle particleType;
    ParticleData particleData;
    boolean longDistance = false;
    double x;
    double y;
    double z;
    float offsetX;
    float offsetY;
    float offsetZ;
    float particleDataFloat;
    int count;

    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                particleType = Particles.byIdentifier(buffer.readString());
                x = buffer.readFloat();
                y = buffer.readFloat();
                z = buffer.readFloat();

                // offset
                offsetX = buffer.readFloat();
                offsetY = buffer.readFloat();
                offsetZ = buffer.readFloat();

                particleDataFloat = buffer.readFloat();
                count = buffer.readInt();
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                particleType = Particles.byId(buffer.readInt(), buffer.getVersion());
                longDistance = buffer.readBoolean();
                x = buffer.readFloat();
                y = buffer.readFloat();
                z = buffer.readFloat();

                // offset
                offsetX = buffer.readFloat();
                offsetY = buffer.readFloat();
                offsetZ = buffer.readFloat();

                particleDataFloat = buffer.readFloat();
                count = buffer.readInt();
                particleData = buffer.readParticleData(particleType);
                return true;
            default:
                particleType = Particles.byId(buffer.readInt(), buffer.getVersion());
                longDistance = buffer.readBoolean();
                x = buffer.readDouble();
                y = buffer.readDouble();
                z = buffer.readDouble();

                // offset
                offsetX = buffer.readFloat();
                offsetY = buffer.readFloat();
                offsetZ = buffer.readFloat();

                particleDataFloat = buffer.readFloat();
                count = buffer.readInt();
                particleData = buffer.readParticleData(particleType);
                return true;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received particle spawn at %s %s %s (offsetX=%s, offsetY=%s, offsetZ=%s, particleType=%s, dataFloat=%s, count=%d, particleData=%s)", x, y, z, offsetX, offsetY, offsetZ, particleType, particleDataFloat, count, particleData));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
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
