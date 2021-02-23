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

import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.modding.event.events.ParticleSpawnEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketParticle extends ClientboundPacket {
    Particle particleType;
    ParticleData particleData;
    boolean longDistance;
    Location location;
    float offsetX;
    float offsetY;
    float offsetZ;
    float particleDataFloat;
    int count;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W19A) {
            this.particleType = buffer.getConnection().getMapping().getParticleRegistry().get(buffer.readResourceLocation());
        } else {
            this.particleType = buffer.getConnection().getMapping().getParticleRegistry().get(buffer.readInt());
        }
        if (buffer.getVersionId() >= V_14W29A) {
            this.longDistance = buffer.readBoolean();
        }
        if (buffer.getVersionId() < V_1_15_PRE4) {
            this.location = buffer.readSmallLocation();
        } else {
            this.location = buffer.readLocation();
        }

        // offset
        this.offsetX = buffer.readFloat();
        this.offsetY = buffer.readFloat();
        this.offsetZ = buffer.readFloat();

        this.particleDataFloat = buffer.readFloat();
        this.count = buffer.readInt();
        this.particleData = buffer.readParticleData(this.particleType);
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new ParticleSpawnEvent(connection, this))) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received particle spawn packet (location=%s, offsetX=%s, offsetY=%s, offsetZ=%s, particleType=%s, dataFloat=%s, count=%d, particleData=%s)", this.location, this.offsetX, this.offsetY, this.offsetZ, this.particleType, this.particleDataFloat, this.count, this.particleData));
    }

    public Location getLocation() {
        return this.location;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }

    public int getCount() {
        return this.count;
    }

    public Particle getParticleType() {
        return this.particleType;
    }

    public ParticleData getParticleData() {
        return this.particleData;
    }

    public float getParticleDataFloat() {
        return this.particleDataFloat;
    }

    public boolean isLongDistance() {
        return this.longDistance;
    }
}
