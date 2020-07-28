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

package de.bixilon.minosoft.game.datatypes.objectLoader.motives;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;
import java.util.HashSet;

public class Motives {


    static HashSet<Motive> motiveList = new HashSet<>();
    static HashBiMap<String, Motive> motiveIdentifierMap = HashBiMap.create();
    static HashMap<ProtocolVersion, HashBiMap<Integer, Motive>> motiveMap = new HashMap<>();

    public static Motive byId(int id, ProtocolVersion version) {
        return motiveMap.get(version).get(id);
    }

    // <= 1.12.2
    public static Motive byIdentifier(String name) {
        return motiveIdentifierMap.get(name);
    }

    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, Motive> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            Motive motive = new Motive(mod, identifierName);
            motiveList.add(motive);
            JsonObject identifierJSON = json.getAsJsonObject(identifierName);
            if (json.getAsJsonObject(identifierName).has("id")) {
                versionMapping.put(json.getAsJsonObject(identifierName).get("id").getAsInt(), motive);
            }
            motiveIdentifierMap.put(mod + ":" + identifierName, motive);
        }
        motiveMap.put(version, versionMapping);
    }

}