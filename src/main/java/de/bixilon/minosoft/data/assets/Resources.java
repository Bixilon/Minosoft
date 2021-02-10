package de.bixilon.minosoft.data.assets;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.util.Util;

import java.io.IOException;
import java.util.Map;

public class Resources {
    private static final HashBiMap<Version, AssetVersion> ASSETS_VERSIONS = HashBiMap.create();

    public static void load() throws IOException {
        JsonObject json = Util.readJsonAssetResource("mapping/resources.json");

        JsonObject versions = json.getAsJsonObject("versions");
        for (Map.Entry<String, JsonElement> versionEntry : versions.entrySet()) {
            loadVersion(versionEntry.getKey(), versionEntry.getValue().getAsJsonObject());
        }

    }

    public static void loadVersion(String versionName, JsonObject json) {
        Version version = Versions.getVersionByName(versionName);
        if (version == null) {
            // version not supported
            return;
        }
        loadVersion(version, json);
    }

    public static void loadVersion(Version version, JsonObject json) {
        String indexVersion = json.has("index_version") ? json.get("index_version").getAsString() : null;
        String indexHash = json.has("index_hash") ? json.get("index_hash").getAsString() : null;
        String clientJarHash = json.has("client_jar_hash") ? json.get("client_jar_hash").getAsString() : null;
        String jarAssetsHash = json.has("jar_assets_hash") ? json.get("jar_assets_hash").getAsString() : null;
        String minosoftMapping = json.has("mappings") ? json.get("mappings").getAsString() : null;

        AssetVersion assetVersion = new AssetVersion(version, indexVersion, indexHash, clientJarHash, jarAssetsHash, minosoftMapping);
        ASSETS_VERSIONS.put(version, assetVersion);
    }

    public static AssetVersion getAssetVersionByVersion(Version version) {
        return ASSETS_VERSIONS.get(version);
    }
}
