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

package de.bixilon.minosoft.game.datatypes.objectLoader.statistics;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Statistics {

    static ArrayList<Statistic> statisticList = new ArrayList<>();
    static HashMap<ProtocolVersion, HashBiMap<Integer, Statistic>> statisticsIdMap = new HashMap<>();
    static HashMap<ProtocolVersion, HashBiMap<String, Statistic>> statisticsIdentifierMap = new HashMap<>();

    public static Statistic getStatisticById(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return statisticsIdMap.get(version).get(protocolId);
    }

    public static Statistic getStatisticByIdentifier(String identifier, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return statisticsIdentifierMap.get(version).get(identifier);
    }

    public static void load(String mod, JSONObject json, ProtocolVersion version) {
        HashBiMap<Integer, Statistic> versionIdMapping = HashBiMap.create();
        HashBiMap<String, Statistic> versionIdentifierMapping = HashBiMap.create();
        for (Iterator<String> identifiers = json.keys(); identifiers.hasNext(); ) {
            String identifierName = identifiers.next();
            Statistic statistic = new Statistic(mod, identifierName);
            if (statisticList.contains(statistic)) {
                statistic = statisticList.get(statisticList.indexOf(statistic));
            }
            if (json.getJSONObject(identifierName).has("id")) {
                versionIdMapping.put(json.getJSONObject(identifierName).getInt("id"), statistic);
            } else {
                versionIdentifierMapping.put(identifierName, statistic);
            }
        }
        statisticsIdMap.put(version, versionIdMapping);
        statisticsIdentifierMap.put(version, versionIdentifierMapping);
    }

}
