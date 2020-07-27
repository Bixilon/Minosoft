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

package de.bixilon.minosoft.game.datatypes.objectLoader.particle;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;
import java.util.HashSet;

public class Particles {

    static HashSet<Particle> particleList = new HashSet<>();
    static HashBiMap<String, Particle> particleIdentifierMap = HashBiMap.create();
    static HashMap<ProtocolVersion, HashBiMap<Integer, Particle>> particleMap = new HashMap<>();

    public static Particle byIdentifier(String name) {
        return particleIdentifierMap.get(name);
    }

    public static Particle byId(int id, ProtocolVersion version) {
        return particleMap.get(version).get(id);
    }

    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, Particle> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            Particle particle = new Particle(mod, identifierName);
            particleList.add(particle);
            JsonObject identifierJSON = json.getAsJsonObject(identifierName);
            versionMapping.put(json.getAsJsonObject(identifierName).get("id").getAsInt(), particle);
            particleIdentifierMap.put(mod + ":" + identifierName, particle);
        }
        particleMap.put(version, versionMapping);
    }
}
