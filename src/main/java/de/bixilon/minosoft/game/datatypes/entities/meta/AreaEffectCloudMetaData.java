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
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getFloat(5, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getFloat(6, defaultValue);
        }
        return sets.getFloat(7, defaultValue);
    }

    public int getColor() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getInt(6, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getInt(7, defaultValue);
        }
        return sets.getInt(8, defaultValue);
    }

    public boolean ignoreRadius() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBoolean(7, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBoolean(8, defaultValue);
        }
        return sets.getBoolean(9, defaultValue);
    }

    public Particle getParticle() {
        final Particle defaultValue = new OtherParticles(Particles.EFFECT);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return new OtherParticles(Particles.byId(sets.getInt(8, defaultValue.getParticle().getId())));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return new OtherParticles(Particles.byId(sets.getInt(9, defaultValue.getParticle().getId())));
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getParticle(9, defaultValue);
        }
        return sets.getParticle(10, defaultValue);
    }

    public int getParticleParameter1() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return sets.getInt(10, defaultValue);
        }
        return defaultValue;
    }

    public int getParticleParameter2() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return sets.getInt(11, defaultValue);
        }
        return defaultValue;
    }
}
