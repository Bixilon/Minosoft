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
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
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
    private val profile = ResourcesProfileManager.selected
    override val namespaces: MutableSet<String> = mutableSetOf()
    private val assetsMapping: MutableMap<ResourceLocation, String> = mutableMapOf()
    private val assetsSizes: MutableMap<String, Long> = mutableMapOf()

    fun downloadAllAssets(latch: CountUpAndDownLatch?) {
        if (this.assetsMapping.isNotEmpty()) {
            return
        }
        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Verifying (and downloading missing) assets...This might take a while" }
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

    private fun getAssetPath(resourceLocation: ResourceLocation, compress: Boolean = true): String {
        return AssetsUtil.getAssetDiskPath(getAssetHash(resourceLocation), compress)
    }

    fun readAssetAsStream(hash: String, compressed: Boolean = true): InputStream {
        var inputStream: InputStream = FileInputStream(AssetsUtil.getAssetDiskPath(hash, compressed))
        if (compressed) {
            inputStream = GZIPInputStream(inputStream)
        }
        return inputStream
    }

    fun generateJarAssets(): String {
        val startTime = KUtil.time
        this.assetVersion.jarAssetsHash?.let {
            if (verifyAssetHash(it)) {
                // ToDo: Verify all jar assets
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "client.jar assets probably already generated for ${assetVersion.version}, skipping" }
                return this.assetVersion.jarAssetsHash
            }
        }

        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Generating client.jar assets for ${assetVersion.version}" }
        // download jar
        downloadAsset(Util.formatString(
            profile.source.launcherPackages,
            mapOf(
                "fullHash" to this.assetVersion.clientJarHash,
                "filename" to "client.jar",
            )
        ), this.assetVersion.clientJarHash!!, true)
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
            val hash: String = FileAssetsManager.saveAsset(versionJar)
            clientJarAssetsHashMap[currentZipEntry.name.substring("assets/".length)] = hash
        }
        val clientJarAssetsMapping = JsonObject()
        for ((path, hash) in clientJarAssetsHashMap) {
            clientJarAssetsMapping.addProperty(path, hash)
        }
        val json = Util.GSON.toJson(clientJarAssetsMapping)
        val assetHash: String = FileAssetsManager.saveAsset(json.toByteArray())
        Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Generated client.jar assets for ${assetVersion.version} in ${KUtil.time - startTime}ms (elements=${clientJarAssetsHashMap.size}, hash=$assetHash" }
        return assetHash
    }

    private fun downloadAssetsIndex() {
        Util.downloadFileAsGz(Util.formatString(
            profile.source.mojangPackages,
            mapOf(
                "fullHash" to this.assetVersion.indexHash,
                "filename" to "${assetVersion.indexVersion}.json",
            )
        ), AssetsUtil.getAssetDiskPath(assetVersion.indexHash!!, true))
    }

    private fun downloadAsset(source: AssetsSource, hash: String) {
        when (source) {
            AssetsSource.MINECRAFT -> {
                downloadAsset(Util.formatString(
                    profile.source.minecraftResources,
                    mapOf(
                        "hashPrefix" to hash.substring(0, 2),
                        "fullHash" to hash,
                    )
                ), hash, true)
            }
            AssetsSource.PIXLYZER -> {
                downloadAsset(Util.formatString(
                    profile.source.pixlyzer,
                    mapOf(
                        "hashPrefix" to hash.substring(0, 2),
                        "fullHash" to hash,
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
            DefaultThreadPool += {
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
        return parseAssetsIndex(Util.readJsonFromStream(readAssetAsStream(hash), true))
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

    override fun getFileAssetSize(hash: String, compress: Boolean): Long {
        val file = File(AssetsUtil.getAssetDiskPath(hash, compress))
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
