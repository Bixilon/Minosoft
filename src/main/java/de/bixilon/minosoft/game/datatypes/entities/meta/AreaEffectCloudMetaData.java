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
package de.bixilon.minosoft.game.datatypes.entities.meta;

import de.bixilon.minosoft.game.datatypes.particle.OtherParticles;
import de.bixilon.minosoft.game.datatypes.particle.Particle;
import de.bixilon.minosoft.game.datatypes.particle.Particles;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class AreaEffectCloudMetaData extends EntityMetaData {

    public AreaEffectCloudMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    public float getRadius() {
        final float defaultValue = 0.5F;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getFloat(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getColor() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public boolean ignoreRadius() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    public Particle getParticle() {
        final Particle defaultValue = new OtherParticles(Particles.EFFECT);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return new OtherParticles(Particles.byId(sets.getInt(super.getLastDataIndex() + 4, defaultValue.getParticle().getId())));
        }
        return sets.getParticle(super.getLastDataIndex() + 4, defaultValue);
    }

    public int getParticleParameter1() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return sets.getInt(super.getLastDataIndex() + 5, defaultValue);
        }
        return defaultValue;
    }

    public int getParticleParameter2() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return sets.getInt(super.getLastDataIndex() + 6, defaultValue);
        }
        return defaultValue;
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return super.getLastDataIndex();
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return super.getLastDataIndex() + 6;
        }
        return super.getLastDataIndex() + 4;
    }
}
