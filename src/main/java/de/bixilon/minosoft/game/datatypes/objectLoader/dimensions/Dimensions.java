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

package de.bixilon.minosoft.game.datatypes.objectLoader.dimensions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class Dimensions {
    static HashMap<ProtocolVersion, HashBiMap<Integer, Dimension>> dimensionIdMap = new HashMap<>(); // version -> (protocolId > Dimension)
    static HashBiMap<String, Dimension> dimensionIdentifierMap = HashBiMap.create(); // Identifier, Dimension
    static HashMap<String, HashMap<String, Dimension>> customDimensionIdentifierMap = new HashMap<>(); // Mod -> (Identifier, Dimension): used > 1.16


    public static Dimension byId(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return dimensionIdMap.get(version).get(protocolId);
    }

    public static Dimension byIdentifier(String identifier) {
        String[] splitted = identifier.split(":", 2);
        return byIdentifier(splitted[0], splitted[1]);
    }

    public static Dimension byIdentifier(String mod, String identifier) {
        if (mod == "minecraft") {
            return dimensionIdentifierMap.get(identifier);
        }
        if (customDimensionIdentifierMap.containsKey(mod)) {
            return customDimensionIdentifierMap.get(mod).get(identifier);
        }
        return null;
    }


    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, Dimension> versionIdMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            JsonObject identifierJSON = json.getAsJsonObject(identifierName);
            Dimension dimension = new Dimension(mod, identifierName, identifierJSON.get("has_skylight").getAsBoolean());
            if (identifierJSON.has("id")) {
                versionIdMapping.put(identifierJSON.get("id").getAsInt(), dimension);
                continue;
            }
            dimensionIdentifierMap.put(identifierName, dimension);
        }
        if (versionIdMapping.size() > 0) {
            dimensionIdMap.put(version, versionIdMapping);
        }
    }
}
