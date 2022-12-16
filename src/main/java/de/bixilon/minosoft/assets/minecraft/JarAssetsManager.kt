/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.assets.minecraft

import com.fasterxml.jackson.databind.JsonNode
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.minosoft.assets.InvalidAssetException
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil.readArchive
import de.bixilon.minosoft.assets.util.FileUtil.readZipArchive
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.generalize
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.kamranzafar.jtar.TarEntry
import org.kamranzafar.jtar.TarHeader
import org.kamranzafar.jtar.TarOutputStream
import java.io.*


/**
 * Integrated assets-manager, that provides the assets in the minecraft.jar file
 * First downloads the original minecraft jar, then removes all unnecessary files
 * Then stores it in another archive (just done once)
 */
class JarAssetsManager(
    val jarAssetsHash: String,
    val clientJarHash: String,
    val profile: ResourcesProfile,
    val version: Version,
    val expectedTarBytes: Int = DEFAULT_TAR_BYTES,
) : MinecraftAssetsManager {
    override var loaded: Boolean = false
        private set
    override val namespaces = setOf(ProtocolDefinition.DEFAULT_NAMESPACE)
    private var jarAssets: MutableMap<String, ByteArray> = mutableMapOf()

    override fun load(latch: CountUpAndDownLatch) {
        check(!loaded) { "Already loaded!" }

        val jarAssets = FileAssetsUtil.readVerified(jarAssetsHash, profile.verify)?.readArchive()
        if (jarAssets != null) {
            for ((path, data) in jarAssets) {
                this.jarAssets[path.removePrefix("assets/" + ProtocolDefinition.DEFAULT_NAMESPACE + "/")] = data
            }
        } else {
            var clientJar = FileAssetsUtil.readVerified(clientJarHash, profile.verify)?.readZipArchive()
            if (clientJar == null) {
                Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloading minecraft jar ($clientJarHash)" }
                val downloaded = FileAssetsUtil.downloadAndGetAsset(
                    profile.source.pistonObjects.formatPlaceholder(
                        "fullHash" to clientJarHash,
                        "filename" to "client.jar",
                    ), false, FileAssetsUtil.HashTypes.SHA1
                )
                check(downloaded.first == clientJarHash) { "Minecraft client.jar verification failed!" }
                clientJar = ByteArrayInputStream(downloaded.second).readZipArchive()
            }

            val buildingJarAsset: MutableMap<String, ByteArray> = mutableMapOf()
            val byteOutputStream = ByteArrayOutputStream(expectedTarBytes)
            val tarOutputStream = TarOutputStream(byteOutputStream)
            for ((filename, data) in clientJar) {
                if (!filename.startsWith("assets/")) {
                    continue
                }
                var cutFilename = filename.removePrefix("assets/")
                val splitFilename = cutFilename.split("/", limit = 2)
                if (splitFilename[0] != ProtocolDefinition.DEFAULT_NAMESPACE) {
                    continue
                }
                cutFilename = splitFilename.getOrNull(1) ?: continue

                var outData = data
                if (cutFilename.endsWith(".json")) {
                    // minify json
                    val jsonNode = Jackson.MAPPER.readValue(data, JsonNode::class.java)
                    outData = jsonNode.toString().toByteArray()
                }

                var required = false
                for (prefix in REQUIRED_FILE_PREFIXES) {
                    if (cutFilename.startsWith(prefix)) {
                        required = true
                        break
                    }
                }
                if (!required) {
                    continue
                }
                buildingJarAsset[cutFilename] = outData
                tarOutputStream.putNextEntry(TarEntry(TarHeader.createHeader(filename, outData.size.toLong(), 0L, false, 777).apply { generalize() }))
                tarOutputStream.write(outData)
                tarOutputStream.flush()
            }
            tarOutputStream.close()
            val tarBytes = byteOutputStream.toByteArray()
            val savedHash = FileAssetsUtil.saveAsset(tarBytes)
            File(FileAssetsUtil.getPath(clientJarHash)).delete()
            if (savedHash != jarAssetsHash) {
                throw InvalidAssetException("jar_assets".toResourceLocation(), savedHash, jarAssetsHash, tarBytes.size)
            }

            this.jarAssets = buildingJarAsset
        }
        loaded = true
    }

    override fun get(path: ResourceLocation): InputStream {
        check(path.namespace == ProtocolDefinition.DEFAULT_NAMESPACE) { "Jar Assets manager does not provide non-minecraft assets!" }
        return ByteArrayInputStream(jarAssets[path.path] ?: throw FileNotFoundException("Can not find asset: $path"))
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        if (path.namespace != ProtocolDefinition.DEFAULT_NAMESPACE) {
            return null
        }
        return ByteArrayInputStream(jarAssets[path.path] ?: return null)
    }

    override fun unload() {
        jarAssets = mutableMapOf()
        loaded = false
    }

    override fun contains(path: ResourceLocation): Boolean {
        if (path.namespace != ProtocolDefinition.DEFAULT_NAMESPACE) {
            return false
        }
        return path.path in jarAssets
    }

    companion object {
        const val DEFAULT_TAR_BYTES = 10_000_000
        private val REQUIRED_FILE_PREFIXES = arrayOf(
            "blockstates/",
            "font/",
            "lang/",
            "models/",
            "particles/",
            "texts/",
            "textures/",
            "recipes/",
        )
    }
}
