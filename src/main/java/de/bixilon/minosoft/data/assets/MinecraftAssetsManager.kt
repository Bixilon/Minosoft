/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.assets

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MinecraftAssetsManager(
    private val assetVersion: AssetVersion,
    private val pixlyzerHash: String,
) : FileAssetsManager {
    override val namespaces: MutableSet<String> = mutableSetOf()
    private val assetsMapping: MutableMap<ResourceLocation, String> = mutableMapOf()
    private val assetsSizes: MutableMap<String, Long> = mutableMapOf()

    fun downloadAllAssets(latch: CountUpAndDownLatch?) {
        if (this.assetsMapping.isNotEmpty()) {
            return
        }
        // download minecraft assets
        if (!verifyAssetHash(assetVersion.indexHash!!)) {
            downloadAssetsIndex()
        }
        this.assetsMapping.putAll(verifyAssets(AssetsSource.MINECRAFT, latch, parseAssetsIndex(assetVersion.indexHash)))

        // generate jar assets index
        generateJarAssets()
        this.assetsMapping.putAll(parseAssetsIndex(assetVersion.jarAssetsHash!!))

        // download pixlyzer mappings
        downloadAsset(AssetsSource.PIXLYZER, pixlyzerHash)
    }

    private fun getAssetHash(resourceLocation: ResourceLocation): String {
        return assetsMapping[resourceLocation] ?: throw FileNotFoundException("Can not find asset: $resourceLocation")
    }

    private fun getAssetPath(resourceLocation: ResourceLocation): String {
        return FileAssetsManager.getAssetDiskPath(getAssetHash(resourceLocation))
    }

    fun readAssetAsStream(hash: String): InputStream {
        return GZIPInputStream(FileInputStream(FileAssetsManager.getAssetDiskPath(hash)))
    }

    fun generateJarAssets(): String {
        val startTime = System.currentTimeMillis()
        this.assetVersion.jarAssetsHash?.let {
            if (verifyAssetHash(it)) {
                // ToDo: Verify all jar assets
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "client.jar assets probably already generated for ${assetVersion.version}, skipping" }
                return this.assetVersion.jarAssetsHash
            }
        }

        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Generating client.jar assets for ${assetVersion.version}" }
        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Generating client.jar assets for ${assetVersion.version}" }
        // download jar
        downloadAsset(String.format(ProtocolDefinition.MOJANG_LAUNCHER_URL_PACKAGES, this.assetVersion.clientJarHash, "client.jar"), this.assetVersion.clientJarHash!!, true)
        val clientJarAssetsHashMap = HashMap<String, String>()
        val versionJar = ZipInputStream(readAssetAsStream(this.assetVersion.clientJarHash))
        var nextZipEntry: ZipEntry?
        while (versionJar.nextEntry.also { nextZipEntry = it } != null) {
            val currentZipEntry = nextZipEntry!!
            if (!currentZipEntry.name.startsWith("assets") || currentZipEntry.isDirectory) {
                continue
            }
            var relevant = false
            for (prefix in ProtocolDefinition.RELEVANT_MINECRAFT_ASSETS) {
                if (currentZipEntry.name.startsWith("assets/$prefix")) {
                    relevant = true
                    break
                }
            }
            if (!relevant) {
                continue
            }
            val hash: String = saveAsset(versionJar)
            clientJarAssetsHashMap[currentZipEntry.name.substring("assets/".length)] = hash
        }
        val clientJarAssetsMapping = JsonObject()
        for ((path, hash) in clientJarAssetsHashMap) {
            clientJarAssetsMapping.addProperty(path, hash)
        }
        val json = Util.GSON.toJson(clientJarAssetsMapping)
        val assetHash: String = saveAsset(json.toByteArray())
        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Generated client.jar assets for ${assetVersion.version} in ${System.currentTimeMillis() - startTime}ms (elements=${clientJarAssetsHashMap.size}, hash=$assetHash" }
        return assetHash
    }

    private fun downloadAssetsIndex() {
        Util.downloadFileAsGz(String.format(ProtocolDefinition.MOJANG_URL_PACKAGES + ".json", assetVersion.indexHash, assetVersion.indexVersion), FileAssetsManager.getAssetDiskPath(assetVersion.indexHash!!))
    }

    private fun downloadAsset(source: AssetsSource, hash: String) {
        when (source) {
            AssetsSource.MINECRAFT -> {
                downloadAsset(String.format(ProtocolDefinition.MINECRAFT_URL_RESOURCES, hash.substring(0, 2), hash), hash, true)
            }
            AssetsSource.PIXLYZER -> {
                downloadAsset(Util.formatString(
                    Minosoft.getConfig().config.download.url.pixlyzer,
                    mapOf(
                        "hashPrefix" to hash.substring(0, 2),
                        "fullHash" to hash
                    )
                ), hash, compress = false, checkURL = false)
            }
            else -> {
            }
        }
    }

    private fun verifyAssets(source: AssetsSource, latch: CountUpAndDownLatch?, assets: Map<ResourceLocation, String>): Map<ResourceLocation, String> {
        val assetsLatch = CountUpAndDownLatch(assets.size, latch)
        for (hash in assets.values) {
            Minosoft.THREAD_POOL.execute {
                // Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE){"Assets, total=${assets.size}, latchTotal=${assetsLatch.total}, current=${assetsLatch.count}"}
                val compressed = source != AssetsSource.PIXLYZER
                if (StaticConfiguration.DEBUG_SLOW_LOADING) {
                    Thread.sleep(100L)
                }
                if (!verifyAssetHash(hash, compressed = compressed)) {
                    downloadAsset(source, hash)
                }
                assetsLatch.dec()
            }
        }

        assetsLatch.awaitWithChange()
        return assets
    }

    private fun parseAssetsIndex(hash: String): Map<ResourceLocation, String> {
        return parseAssetsIndex(Util.readJsonFromStream(readAssetAsStream(hash)))
    }

    private fun parseAssetsIndex(json: JsonObject): Map<ResourceLocation, String> {
        var json = json
        json["objects"]?.asJsonObject?.let {
            json = it
        }

        val ret: MutableMap<ResourceLocation, String> = mutableMapOf()
        for ((location, data) in json.entrySet()) {
            try {
                val resourceLocation = ResourceLocation.getPathResourceLocation(location)
                namespaces.add(resourceLocation.namespace)
                ret[resourceLocation] = if (data is JsonPrimitive) {
                    data.asString
                } else {
                    val hash = data.asJsonObject["hash"].asString
                    data.asJsonObject["size"]?.asLong?.let {
                        assetsSizes[hash] = it // ToDo: Return this somehow
                    }
                    hash
                }
            } catch (exception: Exception) {
            }
        }
        return ret.toMap()
    }

    override fun getAssetSize(hash: String): Long {
        val fileSize = getFileAssetSize(hash)
        if (fileSize < 0) {
            return -1L
        }
        return assetsSizes[hash] ?: -1L // ToDo: Get real size
    }

    override fun getFileAssetSize(hash: String): Long {
        val file = File(FileAssetsManager.getAssetDiskPath(hash))
        return if (file.exists()) {
            file.length()
        } else {
            -1
        }
    }

    override fun getAssetSize(resourceLocation: ResourceLocation): Long {
        return getAssetSize(getAssetHash(resourceLocation))
    }

    override fun getAssetURL(resourceLocation: ResourceLocation): URL {
        TODO()
    }

    override fun readAssetAsStream(resourceLocation: ResourceLocation): InputStream {
        return readAssetAsStream(getAssetHash(resourceLocation))
    }

}
