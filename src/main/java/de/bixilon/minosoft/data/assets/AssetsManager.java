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

package de.bixilon.minosoft.data.assets;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetsManager {
    private static final String[] RELEVANT_ASSETS = {"minecraft/lang/", "minecraft/sounds.json", "minecraft/sounds/", "minecraft/textures/", "minecraft/font/"}; // whitelist for all assets we care (we have our own block models, etc)
    private final boolean verifyHash;
    private final AssetVersion assetVersion;
    private final HashMap<String, String> assetsMap = new HashMap<>();

    public AssetsManager(boolean verifyHash, AssetVersion assetVersion) {
        this.verifyHash = verifyHash;
        this.assetVersion = assetVersion;
    }

    public static InputStreamReader readAssetByHash(String hash) throws IOException {
        return new InputStreamReader(readAssetAsStreamByHash(hash));
    }

    public static InputStream readAssetAsStreamByHash(String hash) throws IOException {
        return new GZIPInputStream(new FileInputStream(getAssetDiskPath(hash)));
    }

    public static JsonElement readJsonAssetByHash(String hash) throws IOException {
        return JsonParser.parseReader(readAssetByHash(hash));
    }

    private static long getAssetSize(String hash) throws FileNotFoundException {
        File file = new File(getAssetDiskPath(hash));
        if (!file.exists()) {
            return -1;
        }
        return file.length();
    }

    private static String saveAsset(byte[] data) throws IOException {
        String hash = Util.sha1(data);
        String destination = getAssetDiskPath(hash);
        File outFile = new File(destination);
        if (outFile.exists() && outFile.length() > 0) {
            return hash;
        }
        Util.createParentFolderIfNotExist(destination);
        OutputStream out = new GZIPOutputStream(new FileOutputStream(destination));
        out.write(data);
        out.close();
        return hash;
    }

    /**
     * Downloads/Copies an asset from a given stream
     *
     * @param data: Data to save
     * @return SHA-1 hash of file
     * @throws IOException On error
     */
    private static String saveAsset(InputStream data) throws IOException {
        File tempDestinationFile = null;
        while (tempDestinationFile == null || tempDestinationFile.exists()) { // file exist? lol
            tempDestinationFile = new File(StaticConfiguration.TEMPORARY_FOLDER + "minosoft/" + Util.generateRandomString(32));
        }
        Util.createParentFolderIfNotExist(tempDestinationFile);

        OutputStream out = new GZIPOutputStream(new FileOutputStream(tempDestinationFile));
        MessageDigest crypt;
        try {
            crypt = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        byte[] buffer = new byte[ProtocolDefinition.DEFAULT_BUFFER_SIZE];
        int length;
        while ((length = data.read(buffer, 0, buffer.length)) != -1) {
            crypt.update(buffer, 0, length);
            out.write(buffer, 0, length);
        }
        out.close();
        String hash = Util.byteArrayToHexString(crypt.digest());

        // move file to desired destination
        File outputFile = new File(getAssetDiskPath(hash));
        Util.createParentFolderIfNotExist(outputFile);
        if (outputFile.exists()) {
            // file is already extracted
            if (!tempDestinationFile.delete()) {
                throw new RuntimeException(String.format("Could not delete temporary file %s", tempDestinationFile.getAbsolutePath()));
            }
            return hash;
        }
        if (!tempDestinationFile.renameTo(outputFile)) {
            throw new RuntimeException(String.format("Could not rename file %s to %s", tempDestinationFile.getAbsolutePath(), outputFile.getAbsolutePath()));
        }
        return hash;
    }

    private static String getAssetDiskPath(String hash) throws FileNotFoundException {
        if (hash == null) {
            throw new FileNotFoundException("Could not find asset with hash: null");
        }
        return StaticConfiguration.HOME_DIRECTORY + String.format("assets/objects/%s/%s.gz", hash.substring(0, 2), hash);
    }

    private boolean verifyAssetHash(String hash, boolean compressed) throws FileNotFoundException {
        // file does not exist
        if (getAssetSize(hash) == -1) {
            return false;
        }
        if (!this.verifyHash) {
            return true;
        }
        try {
            if (compressed) {
                return hash.equals(Util.sha1Gzip(new File(getAssetDiskPath(hash))));
            }
            return hash.equals(Util.sha1(new File(getAssetDiskPath(hash))));
        } catch (IOException ignored) {
        }
        return false;
    }

    private boolean verifyAssetHash(String hash) {
        try {
            return verifyAssetHash(hash, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void downloadAsset(String url, String hash) throws IOException {
        downloadAsset(url, hash, true);
    }

    private void downloadAsset(String url, String hash, boolean compressed) throws IOException {
        if (verifyAssetHash(hash)) {
            return;
        }
        Util.checkURL(url);
        Log.verbose(String.format("Downloading %s -> %s", url, hash));
        if (compressed) {
            Util.downloadFileAsGz(url, getAssetDiskPath(hash));
            return;
        }
        Util.downloadFile(url, getAssetDiskPath(hash));
    }

    public void downloadAssetsIndex() throws IOException {
        Util.downloadFileAsGz(String.format(ProtocolDefinition.MOJANG_URL_PACKAGES + ".json", this.assetVersion.getIndexHash(), this.assetVersion.getIndexVersion()), getAssetDiskPath(this.assetVersion.getIndexHash()));
    }

    private HashMap<String, String> parseAssetsIndex(String hash) throws IOException {
        return parseAssetsIndex(readJsonAssetByHash(hash).getAsJsonObject());
    }

    private HashMap<String, String> parseAssetsIndex(JsonObject json) {
        if (json.has("objects")) {
            json = json.getAsJsonObject("objects");
        }
        HashMap<String, String> ret = new HashMap<>();
        for (String key : json.keySet()) {
            JsonElement value = json.get(key);
            if (value.isJsonPrimitive()) {
                ret.put(key, value.getAsString());
                continue;
            }
            ret.put(key, value.getAsJsonObject().get("hash").getAsString());
        }
        return ret;
    }

    public void downloadAllAssets(CountUpAndDownLatch latch) throws Exception {
        if (!this.assetsMap.isEmpty()) {
            return;
        }
        // download minecraft assets
        if (!verifyAssetHash(this.assetVersion.getIndexHash())) {
            downloadAssetsIndex();
        }
        this.assetsMap.putAll(verifyAssets(AssetsSource.MOJANG, latch, parseAssetsIndex(this.assetVersion.getIndexHash())));

        // generate jar assets index
        generateJarAssets();

        this.assetsMap.putAll(parseAssetsIndex(this.assetVersion.getJarAssetsHash()));

        // download minosoft mappings

        downloadAsset(AssetsSource.MINOSOFT_GIT, this.assetVersion.getMinosoftMappings());
    }

    private HashMap<String, String> verifyAssets(AssetsSource source, CountUpAndDownLatch latch, HashMap<String, String> assets) {
        latch.addCount(assets.size());
        assets.keySet().parallelStream().forEach((filename) -> {
            try {
                String hash = assets.get(filename);
                boolean compressed = (source == AssetsSource.MOJANG);
                if (StaticConfiguration.DEBUG_SLOW_LOADING) {
                    Thread.sleep(100L);
                }
                if (!verifyAssetHash(hash, compressed)) {
                    downloadAsset(source, hash);
                }
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        return assets;
    }

    private void downloadAsset(AssetsSource source, String hash) throws IOException {
        switch (source) {
            case MINECRAFT -> downloadAsset(String.format(ProtocolDefinition.MINECRAFT_URL_RESOURCES, hash.substring(0, 2), hash), hash);
            case MINOSOFT_GIT -> downloadAsset(String.format(Minosoft.getConfig().getString(ConfigurationPaths.StringPaths.RESOURCES_URL), hash.substring(0, 2), hash), hash, false);
        }
    }

    public String generateJarAssets() throws IOException {
        long startTime = System.currentTimeMillis();
        Log.verbose("Generating client.jar assets for %s...", this.assetVersion.getVersion());
        if (verifyAssetHash(this.assetVersion.getJarAssetsHash())) {
            // ToDo: Verify all jar assets
            Log.verbose("client.jar assets probably already generated for %s, skipping", this.assetVersion.getVersion());
            return this.assetVersion.getJarAssetsHash();
        }
        // download jar
        downloadAsset(String.format(ProtocolDefinition.MOJANG_LAUNCHER_URL_PACKAGES, this.assetVersion.getClientJarHash(), "client.jar"), this.assetVersion.getClientJarHash());

        HashMap<String, String> clientJarAssetsHashMap = new HashMap<>();
        ZipInputStream versionJar = new ZipInputStream(readAssetAsStreamByHash(this.assetVersion.getClientJarHash()));
        ZipEntry currentFile;
        while ((currentFile = versionJar.getNextEntry()) != null) {
            if (!currentFile.getName().startsWith("assets") || currentFile.isDirectory()) {
                continue;
            }
            boolean relevant = false;
            for (String prefix : RELEVANT_ASSETS) {
                if (currentFile.getName().startsWith("assets/" + prefix)) {
                    relevant = true;
                    break;
                }
            }
            if (!relevant) {
                continue;
            }
            String hash = saveAsset(versionJar);

            clientJarAssetsHashMap.put(currentFile.getName().substring("assets/".length()), hash);
        }
        JsonObject clientJarAssetsMapping = new JsonObject();
        clientJarAssetsHashMap.forEach(clientJarAssetsMapping::addProperty);
        String json = new GsonBuilder().create().toJson(clientJarAssetsMapping);
        String assetHash = saveAsset(json.getBytes());
        Log.verbose(String.format("Generated jar assets in %dms (elements=%d, hash=%s)", (System.currentTimeMillis() - startTime), clientJarAssetsHashMap.size(), assetHash));
        return assetHash;
    }

    public boolean doesAssetExist(String name) {
        return this.assetsMap.containsKey(name);
    }

    public HashMap<String, String> getAssetsMap() {
        return this.assetsMap;
    }

    public InputStreamReader readAsset(String name) throws IOException {
        String hash = this.assetsMap.get(name);
        if (hash == null) {
            throw new FileNotFoundException(String.format("Can not find asset with name: %s", name));
        }
        return readAssetByHash(hash);
    }

    public String readStringAsset(String name) throws IOException {
        return Util.readFile(new BufferedReader(readAsset(name)), true);
    }

    public InputStream readAssetAsStream(String name) throws IOException {
        String hash = this.assetsMap.get(name);
        if (hash == null) {
            throw new FileNotFoundException(String.format("Can not find asset with name: %s", name));
        }
        return readAssetAsStreamByHash(hash);
    }

    public InputStream readAssetAsStream(ModIdentifier identifier) throws IOException {
        return readAssetAsStream(identifier.getMod() + "/" + identifier.getIdentifier());
    }

    public JsonElement readJsonAsset(String name) throws IOException {
        return readJsonAssetByHash(this.assetsMap.get(name));
    }

    public AssetVersion getAssetVersion() {
        return this.assetVersion;
    }
}
