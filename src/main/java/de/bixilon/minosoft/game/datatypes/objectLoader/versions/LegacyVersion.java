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

package de.bixilon.minosoft.game.datatypes.objectLoader.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.Mappings;
import de.bixilon.minosoft.game.datatypes.objectLoader.motives.Motive;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.statistics.Statistic;

public class LegacyVersion extends Version {
    HashBiMap<String, Motive> motiveIdentifierMap;
    HashBiMap<String, Particle> particleIdentifierMap;
    HashBiMap<String, Statistic> statisticIdentifierMap;

    public LegacyVersion() {
        super("legacy", -2, null, null);
    }

    @Override
    public Motive getMotiveByIdentifier(String identifier) {
        return motiveIdentifierMap.get(identifier);
    }

    @Override
    public Statistic getStatisticByIdentifier(String identifier) {
        return statisticIdentifierMap.get(identifier);
    }

    @Override
    public Particle getParticleByIdentifier(String identifier) {
        return particleIdentifierMap.get(identifier);
    }

    @Override
    public void load(Mappings type, JsonObject data) {
        super.load(type, data);
    }

    @Override
    public void unload() {
        super.unload();
        motiveIdentifierMap.clear();
        particleIdentifierMap.clear();
        statisticIdentifierMap.clear();
    }
}
