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

package de.bixilon.minosoft.generator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.Configuration;
import de.bixilon.minosoft.data.assets.MinecraftAssetsManager;
import de.bixilon.minosoft.data.assets.Resources;
import de.bixilon.minosoft.data.registries.versions.Version;

import java.io.*;
import java.util.Collections;

public class JarHashGenerator {
    private static final String RESOURCE_JSON_PATH = "src/main/resources/assets/minosoft/mapping/resources.json";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: JarHashGenerator <Version>");
            return;
        }
        try {
            Minosoft.config = new Configuration();


            Version version = new Version(args[0], -1, -1, Collections.emptyMap(), Collections.emptyMap());

            JsonObject json = JsonParser.parseReader(new InputStreamReader(new FileInputStream(RESOURCE_JSON_PATH))).getAsJsonObject();


            JsonObject versions = json.getAsJsonObject("versions");

            JsonObject versionJson = versions.getAsJsonObject(version.getName());

            Resources.loadVersion(version, versionJson);

            var resource = Resources.getAssetVersionByVersion(version);
            MinecraftAssetsManager assetsManager = new MinecraftAssetsManager(resource, "dummy");
            String jarAssetsHash = assetsManager.generateJarAssets();

            versionJson.addProperty("jar_assets_hash", jarAssetsHash);


            // reload json, because the generator is async

            json = JsonParser.parseReader(new InputStreamReader(new FileInputStream(RESOURCE_JSON_PATH))).getAsJsonObject();

            json.getAsJsonObject("versions").add(version.getName(), versionJson);

            File file = new File(RESOURCE_JSON_PATH);
            FileWriter writer = new FileWriter(file.getAbsoluteFile());
            writer.write(new Gson().toJson(json));
            writer.close();
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
