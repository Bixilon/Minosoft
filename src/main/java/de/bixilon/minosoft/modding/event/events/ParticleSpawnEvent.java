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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.ParticleData;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketParticle;

public class ParticleSpawnEvent extends CancelableEvent {
    private final Particle particleType;
    private final ParticleData particleData;
    private final Location location;
    private boolean longDistance;
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private int count;

    public ParticleSpawnEvent(Connection connection, Particle particleType, ParticleData particleData, boolean longDistance, Location location, float offsetX, float offsetY, float offsetZ, int count) {
        super(connection);
        this.particleType = particleType;
        this.particleData = particleData;
        this.longDistance = longDistance;
        this.location = location;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.count = count;
    }

    public ParticleSpawnEvent(Connection connection, Particle particleType, ParticleData particleData, Location location) {
        super(connection);
        this.particleType = particleType;
        this.particleData = particleData;
        this.location = location;
    }

    public ParticleSpawnEvent(Connection connection, PacketParticle pkg) {
        super(connection);
        this.particleType = pkg.getParticleType();
        this.particleData = pkg.getParticleData();
        this.longDistance = pkg.isLongDistance();
        this.location = pkg.getLocation();
        this.offsetX = pkg.getOffsetX();
        this.offsetY = pkg.getOffsetY();
        this.offsetZ = pkg.getOffsetZ();
        this.count = pkg.getCount();
    }

    public Particle getParticleType() {
        return particleType;
    }

    public ParticleData getParticleData() {
        return particleData;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public void setLongDistance(boolean longDistance) {
        this.longDistance = longDistance;
    }

    public Location getLocation() {
        return location;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
