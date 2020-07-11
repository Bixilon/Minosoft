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

package de.bixilon.minosoft.game.datatypes.entities;

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Items {

    // ProtocolVersion-> itemId << 4 | metaData, Identifier
    static HashMap<ProtocolVersion, HashMap<Integer, String>> itemProtocolMap = new HashMap<>();

    public static String getIdentifierByLegacy(int id, int metaData, ProtocolVersion version) {
        return getIdentifier((id << 4 | metaData), version);
    }

    public static String getIdentifier(int id, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return itemProtocolMap.get(version).get(id);
    }

    public static void load(ProtocolVersion version, JSONObject json) throws IOException {
        HashMap<Integer, String> versionMapping = new HashMap<>();
        if (version == ProtocolVersion.VERSION_1_13_2) {
            // old format
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String identifier = it.next();
                versionMapping.put(json.getJSONObject(identifier).getInt("protocol_id"), identifier);
            }
        }
        itemProtocolMap.put(version, versionMapping);
    }

    public static int getItemId(String identifier, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        for (Map.Entry<Integer, String> identifierSet : itemProtocolMap.get(version).entrySet()) {
            if (identifierSet.getValue().equals(identifier)) {
                return identifierSet.getKey();
            }
        }
        return -1;
    }

}
