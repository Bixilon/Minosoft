/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.Mappings;
import de.bixilon.minosoft.data.assets.AssetsManager;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Versions {
    public static final Version LOWEST_VERSION_SUPPORTED = new Version("Automatic", -1, -1, null, null);
    private static final HashBiMap<Integer, Version> VERSION_ID_MAP = HashBiMap.create();
    private static final HashBiMap<Integer, Version> VERSION_PROTOCOL_ID_MAP = HashBiMap.create();
    private static final HashSet<Version> LOADED_VERSIONS = new HashSet<>();
    public static VersionMapping PRE_FLATTENING_MAPPING;

    public static Version getVersionById(int versionId) {
        return VERSION_ID_MAP.get(versionId);
    }

    public static Version getVersionByProtocolId(int protocolId) {
        return VERSION_PROTOCOL_ID_MAP.get(protocolId);
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

        HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
        HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;
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
                Packets.Serverbound packet = Packets.Serverbound.valueOf(packetElement.getAsString());
                if (!serverboundPacketMapping.containsKey(packet.getState())) {
                    serverboundPacketMapping.put(packet.getState(), HashBiMap.create());
                }
                serverboundPacketMapping.get(packet.getState()).put(packet, serverboundPacketMapping.get(packet.getState()).size());
            }
            clientboundPacketMapping = new HashMap<>();
            for (JsonElement packetElement : mappingJson.getAsJsonArray("clientbound")) {
                Packets.Clientbound packet = Packets.Clientbound.valueOf(packetElement.getAsString());
                if (!clientboundPacketMapping.containsKey(packet.getState())) {
                    clientboundPacketMapping.put(packet.getState(), HashBiMap.create());
                }
                clientboundPacketMapping.get(packet.getState()).put(packet, clientboundPacketMapping.get(packet.getState()).size());
            }
        }
        int protocolId = versionId;
        if (versionJson.has("protocolId")) {
            protocolId = versionJson.get("protocolId").getAsInt();
        }
        Version version = new Version(versionName, versionId, protocolId, serverboundPacketMapping, clientboundPacketMapping);
        VERSION_ID_MAP.put(version.getVersionId(), version);
        VERSION_PROTOCOL_ID_MAP.put(version.getProtocolId(), version);
        return version;
    }

    public static void loadVersionMappings(Version version) throws IOException {
        if (version.isLoaded()) {
            // already loaded
            return;
        }
        Version preFlatteningVersion = VERSION_ID_MAP.get(ProtocolDefinition.PRE_FLATTENING_VERSION_ID);
        if (!version.isFlattened() && version != preFlatteningVersion && !preFlatteningVersion.isLoaded()) {
            // no matter what, we need the version mapping for all pre flattening versions
            loadVersionMappings(preFlatteningVersion);
        }
        if (version.isGettingLoaded()) {
            // async: we don't wanna load this version twice, skip
            return;
        }
        version.setGettingLoaded(true);
        Log.verbose(String.format("Loading mappings for version %s...", version));
        long startTime = System.currentTimeMillis();

        HashMap<String, JsonObject> files;
        try {
            files = Util.readJsonTarStream(AssetsManager.readAssetAsStream(String.format("mappings/%s", version.getVersionName())));
        } catch (Exception e) {
            // should not happen, but if this version is not flattened, we can fallback to the flatten mappings. Some things might not work...
            Log.printException(e, LogLevels.VERBOSE);
            if (version.isFlattened() || version.getVersionId() == ProtocolDefinition.FLATTING_VERSION_ID) {
                throw e;
            }
            files = new HashMap<>();
        }

        for (Mappings mapping : Mappings.values()) {
            JsonObject data = null;
            if (files.containsKey(mapping.getFilename() + ".json")) {
                data = files.get(mapping.getFilename() + ".json");
            }
            if (data == null) {
                loadVersionMappings(mapping, ProtocolDefinition.DEFAULT_MOD, null, version);
                continue;
            }
            for (String mod : data.keySet()) {
                loadVersionMappings(mapping, mod, data.getAsJsonObject(mod), version);
            }
        }
        if (!files.isEmpty()) {
            Log.verbose(String.format("Loaded mappings for version %s in %dms (%s)", version, (System.currentTimeMillis() - startTime), version.getVersionName()));
        } else {
            Log.verbose(String.format("Could not load mappings for version %s. Some features will be unavailable.", version));
        }
        version.setGettingLoaded(false);
    }

    public static void loadVersionMappings(Mappings type, String mod, @Nullable JsonObject data, Version version) {
        VersionMapping mapping;
        mapping = version.getMapping();
        if (mapping == null) {
            mapping = new VersionMapping(version);
            version.setMapping(mapping);
        }
        mapping.load(type, mod, data, version);

        if (version.getVersionId() == ProtocolDefinition.PRE_FLATTENING_VERSION_ID && PRE_FLATTENING_MAPPING == null) {
            PRE_FLATTENING_MAPPING = mapping;
        }
        LOADED_VERSIONS.add(version);
    }

    public static HashBiMap<Integer, Version> getVersionIdMap() {
        return VERSION_ID_MAP;
    }
}
