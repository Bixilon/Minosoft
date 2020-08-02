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

import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.ParticleData;

public class AreaEffectCloudMetaData extends EntityMetaData {

    public AreaEffectCloudMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public float getRadius() {
        final float defaultValue = 0.5F;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getFloat(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getColor() {
        final int defaultValue = 0;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public boolean ignoreRadius() {
        final boolean defaultValue = false;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    public ParticleData getParticle() {
        // ToDo: final ParticleData defaultValue = new ParticleData(Particles.byIdentifier("minecraft:effect"));
        final ParticleData defaultValue = null;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        if (protocolId <= 335) { //ToDo
            //ToDo return new ParticleData(Particles.byId(sets.getInt(super.getLastDataIndex() + 4, 0), version));
            return null;
        }
        return sets.getParticle(super.getLastDataIndex() + 4, defaultValue);
    }

    public int getParticleParameter1() {
        final int defaultValue = 0;
        if (protocolId < 204) { //ToDo
            return defaultValue;
        }
        if (protocolId <= 335) { //ToDo
            return sets.getInt(super.getLastDataIndex() + 5, defaultValue);
        }
        return defaultValue;
    }

    public int getParticleParameter2() {
        final int defaultValue = 0;
        if (protocolId < 204) { //ToDo
            return defaultValue;
        }
        if (protocolId <= 335) { //ToDo
            return sets.getInt(super.getLastDataIndex() + 6, defaultValue);
        }
        return defaultValue;
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 110) { //ToDo
            return super.getLastDataIndex();
        }
        if (protocolId <= 335) { //ToDo
            return super.getLastDataIndex() + 6;
        }
        return super.getLastDataIndex() + 4;
    }
}
