package de.bixilon.minosoft.generator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.data.assets.AssetsManager;
import de.bixilon.minosoft.data.assets.Resources;
import de.bixilon.minosoft.data.mappings.versions.Version;

import java.io.*;

public class JarHashGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: JarHashGenerator <Version>");
            return;
        }
        try {
            Version version = new Version(args[0], -1, -1, null, null);

            JsonObject json = JsonParser.parseReader(new InputStreamReader(new FileInputStream("src/main/resources/assets/mapping/resources.json"))).getAsJsonObject();


            JsonObject versions = json.getAsJsonObject("versions");

            JsonObject versionJson = versions.getAsJsonObject(version.getVersionName());

            Resources.loadVersion(version, versionJson);

            AssetsManager assetsManager = new AssetsManager(true, Resources.getAssetVersionByVersion(version));
            String jarAssetsHash = assetsManager.generateJarAssets();

            versionJson.addProperty("jar_assets_hash", jarAssetsHash);


            File file = new File("src/main/resources/assets/mapping/resources.json");
            FileWriter writer = new FileWriter(file.getAbsoluteFile());
            writer.write(new Gson().toJson(json));
            writer.close();
            System.exit(0);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
