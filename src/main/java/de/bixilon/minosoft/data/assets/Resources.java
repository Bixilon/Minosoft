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

package de.bixilon.minosoft.data.assets;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.data.registries.versions.Version;
import de.bixilon.minosoft.data.registries.versions.Versions;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Resources {
    private static final HashBiMap<Version, AssetVersion> ASSETS_VERSIONS = HashBiMap.create();
    private static final HashMap<Version, String> PIXLYZER_VERSIONS = new HashMap<>();

    public static void load() throws IOException {
        JsonObject json = Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(new ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/resources.json"));

        JsonObject versions = json.getAsJsonObject("versions");
        for (Map.Entry<String, JsonElement> versionEntry : versions.entrySet()) {
            Version version = Versions.getVersionByName(versionEntry.getKey());
            if (version == null) {
                // version not supported
                continue;
            }
            loadVersion(version, versionEntry.getValue().getAsJsonObject());
        }

        // PixLyzer
        JsonObject pixlyzerIndex = Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(new ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/pixlyzer_index.json"));

        for (Map.Entry<String, JsonElement> versionEntry : pixlyzerIndex.entrySet()) {

            Version version = Versions.getVersionByName(versionEntry.getKey());
            if (version == null) {
                // version not supported
                continue;
            }
            PIXLYZER_VERSIONS.put(version, versionEntry.getValue().getAsString());
        }

    }

    public static void loadVersion(Version version, JsonObject json) {
        String indexVersion = json.has("index_version") ? json.get("index_version").getAsString() : null;
        String indexHash = json.has("index_hash") ? json.get("index_hash").getAsString() : null;
        String clientJarHash = json.has("client_jar_hash") ? json.get("client_jar_hash").getAsString() : null;
        String jarAssetsHash = json.has("jar_assets_hash") ? json.get("jar_assets_hash").getAsString() : null;

        AssetVersion assetVersion = new AssetVersion(version, indexVersion, indexHash, clientJarHash, jarAssetsHash);
        ASSETS_VERSIONS.put(version, assetVersion);
    }

    public static AssetVersion getAssetVersionByVersion(Version version) {
        return ASSETS_VERSIONS.get(version);
    }

    public static String getPixLyzerDataHashByVersion(Version version) {
        return PIXLYZER_VERSIONS.get(version);
    }
}
