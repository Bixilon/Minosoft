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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.Mappings;
import de.bixilon.minosoft.protocol.protocol.Packets;

import java.util.HashSet;

import static de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.FLATTING_VERSION_ID;

public class Versions {

    final static Version legacyVersion = new LegacyVersion(); // used for 1.7.x - 1.12.2 mapping
    static HashBiMap<Integer, Version> versionMap = HashBiMap.create();
    static HashSet<Version> loadedVersion = new HashSet<>();

    public static Version getVersionById(int protocolId) {
        return versionMap.get(protocolId);
    }

    public static void load(JsonObject json) {
        for (String protocolId : json.keySet()) {
            loadVersion(json, protocolId);
        }
    }

    public static void loadVersion(JsonObject json, String protocolIdString) {
        JsonObject versionJson = json.getAsJsonObject(protocolIdString);
        String versionName = versionJson.get("name").getAsString();
        int protocolId = Integer.parseInt(protocolIdString);
        if (versionMap.containsKey(protocolId)) {
            // already loaded, skip
            return;
        }

        HashBiMap<Packets.Serverbound, Integer> serverboundPacketMapping;
        HashBiMap<Packets.Clientbound, Integer> clientboundPacketMapping;
        if (versionJson.get("mapping").isJsonPrimitive()) {
            // inherits or copies mapping from an other version
            if (!versionMap.containsKey(protocolId)) {
                loadVersion(json, versionJson.get("mapping").getAsString());
            }
            Version parent = versionMap.get(versionJson.get("mapping").getAsInt());
            serverboundPacketMapping = parent.getServerboundPacketMapping();
            clientboundPacketMapping = parent.getClientboundPacketMapping();
        } else {
            JsonObject mappingJson = versionJson.getAsJsonObject("mapping");
            serverboundPacketMapping = HashBiMap.create();

            for (JsonElement packetElement : mappingJson.getAsJsonArray("serverbound")) {
                String packetName = packetElement.getAsString();
                serverboundPacketMapping.put(Packets.Serverbound.valueOf(packetName), serverboundPacketMapping.size());
            }
            clientboundPacketMapping = HashBiMap.create();
            for (JsonElement packetElement : mappingJson.getAsJsonArray("clientbound")) {
                String packetName = packetElement.getAsString();
                clientboundPacketMapping.put(Packets.Clientbound.valueOf(packetName), clientboundPacketMapping.size());
            }
        }
        Version version = new Version(versionName, protocolId, serverboundPacketMapping, clientboundPacketMapping);
        versionMap.put(version.getProtocolVersion(), version);
    }

    public static void loadVersionMappings(Mappings type, JsonObject data, int protocolId) {
        Version version = versionMap.get(protocolId);
        version.load(type, data);
        loadedVersion.add(version);
    }

    public static void unloadUnnecessaryVersions(int necessary) {
        if (necessary >= FLATTING_VERSION_ID) {
            legacyVersion.unload();
        }
        for (Version version : loadedVersion) {
            if (version.getProtocolVersion() == necessary) {
                continue;
            }
            version.unload();
        }
    }

}
