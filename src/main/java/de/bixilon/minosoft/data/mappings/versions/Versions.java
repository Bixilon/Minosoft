/*
 * Minosoft
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

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.data.Mappings;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipException;

public class Versions {
    static final HashBiMap<Integer, Version> versionIdMap = HashBiMap.create();
    static final HashBiMap<Integer, Version> versionProtocolIdMap = HashBiMap.create();
    static final HashSet<Version> loadedVersions = new HashSet<>();
    private static final Version lowestVersionSupported = new Version("Automatic", -1, -1, null, null);
    private static final HashMap<String, Mappings> mappingsHashMap = new HashMap<>();
    static VersionMapping legacyMapping;

    static {
        mappingsHashMap.put("registries", Mappings.REGISTRIES);
        mappingsHashMap.put("blocks", Mappings.BLOCKS);
    }

    public static Version getVersionById(int versionId) {
        return versionIdMap.get(versionId);
    }

    public static Version getVersionByProtocolId(int protocolId) {
        return versionProtocolIdMap.get(protocolId);
    }

    public static void load(JsonObject json) {
        for (String versionId : json.keySet()) {
            loadVersion(json, versionId);
        }
    }

    public static void loadVersion(JsonObject json, String versionIdString) {
        JsonObject versionJson = json.getAsJsonObject(versionIdString);
        String versionName = versionJson.get("name").getAsString();
        int versionId = Integer.parseInt(versionIdString);
        if (versionIdMap.containsKey(versionId)) {
            // already loaded, skip
            return;
        }

        HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
        HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;
        if (versionJson.get("mapping").isJsonPrimitive()) {
            // inherits or copies mapping from an other version
            if (!versionIdMap.containsKey(versionId)) {
                loadVersion(json, versionJson.get("mapping").getAsString());
            }
            Version parent = versionIdMap.get(versionJson.get("mapping").getAsInt());
            serverboundPacketMapping = parent.getServerboundPacketMapping();
            clientboundPacketMapping = parent.getClientboundPacketMapping();
        } else {
            JsonObject mappingJson = versionJson.getAsJsonObject("mapping");
            serverboundPacketMapping = new HashMap<>();

            for (JsonElement packetElement : mappingJson.getAsJsonArray("serverbound")) {
                String packetName = packetElement.getAsString();
                Packets.Serverbound packet = Packets.Serverbound.valueOf(packetName);
                if (!serverboundPacketMapping.containsKey(packet.getState())) {
                    serverboundPacketMapping.put(packet.getState(), HashBiMap.create());
                }
                serverboundPacketMapping.get(packet.getState()).put(packet, serverboundPacketMapping.get(packet.getState()).size());
            }
            clientboundPacketMapping = new HashMap<>();
            for (JsonElement packetElement : mappingJson.getAsJsonArray("clientbound")) {
                String packetName = packetElement.getAsString();
                Packets.Clientbound packet = Packets.Clientbound.valueOf(packetName);
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
        versionIdMap.put(version.getVersionId(), version);
        versionProtocolIdMap.put(version.getProtocolId(), version);
    }

    public static void loadVersionMappings(int versionId) throws IOException {
        Version version = versionIdMap.get(versionId);
        if (version.getMapping() != null && version.getMapping().isFullyLoaded()) {
            // already loaded
            return;
        }
        if (versionId < ProtocolDefinition.FLATTING_VERSION_ID) {
            version = versionIdMap.get(ProtocolDefinition.PRE_FLATTENING_VERSION_ID);
        }
        if (version.isGettingLoaded()) {
            return;
        }
        version.setGettingLoaded(true);
        Log.verbose(String.format("Loading mappings for version %s...", version));
        long startTime = System.currentTimeMillis();

        // check if mapping folder exist
        File mappingFolder = new File(Config.homeDir + "assets/mapping");
        if (!mappingFolder.exists()) {
            if (mappingFolder.mkdirs()) {
                Log.verbose("Created mappings folder.");
            } else {
                Log.fatal(String.format("Failed creating mappings folder (%s). Exiting...", mappingFolder.getAbsolutePath()));
                System.exit(1);
            }
        }

        String fileName = Config.homeDir + String.format("assets/mapping/%s.tar.gz", version.getVersionName());
        HashMap<String, JsonObject> files;
        try {
            files = Util.readJsonTarGzFile(fileName);
        } catch (FileNotFoundException e) {
            long downloadStartTime = System.currentTimeMillis();
            Log.info(String.format("Mappings for %s are not available on disk. Downloading them...", version.getVersionName()));
            Util.downloadFile(String.format(Minosoft.getConfig().getString(ConfigurationPaths.MAPPINGS_URL), version.getVersionName()), fileName);
            try {
                files = Util.readJsonTarGzFile(fileName);
            } catch (ZipException e2) {
                // bullshit downloaded, delete file
                new File(fileName).delete();
                throw e2;
            }
            Log.info(String.format("Mappings for %s downloaded successfully in %dms!", version.getVersionName(), (System.currentTimeMillis() - downloadStartTime)));
        }

        for (Map.Entry<String, Mappings> mappingSet : mappingsHashMap.entrySet()) {
            JsonObject data = files.get(mappingSet.getKey() + ".json").getAsJsonObject("minecraft");
            loadVersionMappings(mappingSet.getValue(), data, versionId);
        }

        Log.verbose(String.format("Loaded mappings for version %s in %dms (%s)", version, (System.currentTimeMillis() - startTime), version.getVersionName()));
        version.setGettingLoaded(false);
    }

    public static void loadVersionMappings(Mappings type, JsonObject data, int versionId) {
        Version version = versionIdMap.get(versionId);
        VersionMapping mapping;
        if (versionId < ProtocolDefinition.FLATTING_VERSION_ID) {
            if (legacyMapping == null) {
                legacyMapping = new VersionMapping(version);
            }
            if (!legacyMapping.isFullyLoaded()) {
                legacyMapping.load(type, data);
            }
            mapping = legacyMapping;
        } else {
            mapping = version.getMapping();
            if (mapping == null) {
                mapping = new VersionMapping(version);
            }
            mapping.load(type, data);
        }
        version.setMapping(mapping);
        loadedVersions.add(version);
    }

    public static Version getLowestVersionSupported() {
        return lowestVersionSupported;
    }

    public static HashBiMap<Integer, Version> getVersionIdMap() {
        return versionIdMap;
    }
}
