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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.mappings.particle.ParticleType;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.ParticleS2CP;
import glm_.vec3.Vec3;

public class ParticleSpawnEvent extends CancelableEvent {
    private final ParticleType particleType;
    private final ParticleData particleData;
    private final Vec3 position;
    private boolean longDistance;
    private Vec3 offset;
    private int count;

    public ParticleSpawnEvent(PlayConnection connection, ParticleType particleType, ParticleData particleData, boolean longDistance, Vec3 position, Vec3 offset, int count) {
        super(connection);
        this.particleType = particleType;
        this.particleData = particleData;
        this.longDistance = longDistance;
        this.position = position;
        this.offset = offset;
        this.count = count;
    }

    public ParticleSpawnEvent(PlayConnection connection, ParticleType particleType, ParticleData particleData, Vec3 position) {
        super(connection);
        this.particleType = particleType;
        this.particleData = particleData;
        this.position = position;
    }

    public ParticleSpawnEvent(PlayConnection connection, ParticleS2CP pkg) {
        super(connection);
        this.particleType = pkg.getType();
        this.particleData = pkg.getParticleData();
        this.longDistance = pkg.getLongDistance();
        this.position = pkg.getPosition();
        this.offset = pkg.getOffset();
        this.count = pkg.getCount();
    }

    public ParticleType getParticleType() {
        return this.particleType;
    }

    public ParticleData getParticleData() {
        return this.particleData;
    }

    public boolean isLongDistance() {
        return this.longDistance;
    }

    public void setLongDistance(boolean longDistance) {
        this.longDistance = longDistance;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public Vec3 getOffset() {
        return this.offset;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
