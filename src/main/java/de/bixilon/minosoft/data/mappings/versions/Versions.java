/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.PacketTypes;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;
import java.util.Map;

public class Versions {
    public static final Version AUTOMATIC_VERSION = new Version("Automatic", -1, -1, Map.of(), Map.of());
    private static final HashBiMap<Integer, Version> VERSION_ID_MAP = HashBiMap.create(500);
    private static final HashBiMap<Integer, Version> VERSION_PROTOCOL_ID_MAP = HashBiMap.create(500);
    private static final HashBiMap<String, Version> VERSION_NAME_MAP = HashBiMap.create(500);
    public static VersionMapping PRE_FLATTENING_MAPPING;
    public static Version PRE_FLATTENING_VERSION;

    public static Version getVersionById(int versionId) {
        return VERSION_ID_MAP.get(versionId);
    }

    public static Version getVersionByProtocolId(int protocolId) {
        return VERSION_PROTOCOL_ID_MAP.get(protocolId);
    }

    public static Version getVersionByName(String name) {
        return VERSION_NAME_MAP.get(name);
    }

    public static void loadAvailableVersions(JsonObject json) {
        for (String versionId : json.keySet()) {
            loadVersion(json, versionId);
        }
    }

    private static Version loadVersion(JsonObject json, String versionIdString) {
        JsonObject versionJson = json.getAsJsonObject(versionIdString);
        String versionName = versionJson.get("name").getAsString();
        int versionId = Integer.parseInt(versionIdString);
        if (VERSION_ID_MAP.containsKey(versionId)) {
            // already loaded, skip
            return VERSION_ID_MAP.get(versionId);
        }

        Map<ConnectionStates, HashBiMap<PacketTypes.Serverbound, Integer>> serverboundPacketMapping;
        Map<ConnectionStates, HashBiMap<PacketTypes.Clientbound, Integer>> clientboundPacketMapping;
        if (versionJson.get("mapping").isJsonPrimitive()) {
            // inherits or copies mapping from an other version
            Version parent = VERSION_ID_MAP.get(versionJson.get("mapping").getAsInt());
            if (parent == null) {
                parent = loadVersion(json, versionJson.get("mapping").getAsString());
            }
            serverboundPacketMapping = parent.getServerboundPacketMapping();
            clientboundPacketMapping = parent.getClientboundPacketMapping();
        } else {
            JsonObject mappingJson = versionJson.getAsJsonObject("mapping");
            serverboundPacketMapping = new HashMap<>();

            for (JsonElement packetElement : mappingJson.getAsJsonArray("serverbound")) {
                PacketTypes.Serverbound packet = PacketTypes.Serverbound.valueOf(packetElement.getAsString());
                if (!serverboundPacketMapping.containsKey(packet.getState())) {
                    serverboundPacketMapping.put(packet.getState(), HashBiMap.create(30));
                }
                serverboundPacketMapping.get(packet.getState()).put(packet, serverboundPacketMapping.get(packet.getState()).size());
            }
            clientboundPacketMapping = new HashMap<>();
            for (JsonElement packetElement : mappingJson.getAsJsonArray("clientbound")) {
                PacketTypes.Clientbound packet = PacketTypes.Clientbound.valueOf(packetElement.getAsString());
                if (!clientboundPacketMapping.containsKey(packet.getState())) {
                    clientboundPacketMapping.put(packet.getState(), HashBiMap.create(100));
                }
                clientboundPacketMapping.get(packet.getState()).put(packet, clientboundPacketMapping.get(packet.getState()).size());
            }
        }
        int protocolId = versionId;
        if (versionJson.has("protocol_id")) {
            protocolId = versionJson.get("protocol_id").getAsInt();
        }
        Version version = new Version(versionName, versionId, protocolId, serverboundPacketMapping, clientboundPacketMapping);
        VERSION_ID_MAP.put(version.getVersionId(), version);
        VERSION_PROTOCOL_ID_MAP.put(version.getProtocolId(), version);
        VERSION_NAME_MAP.put(version.getVersionName(), version);
        if (version.getVersionId() == ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
            PRE_FLATTENING_VERSION = version;
        }
        return version;
    }

    public static HashBiMap<Integer, Version> getVersionIdMap() {
        return VERSION_ID_MAP;
    }
}
