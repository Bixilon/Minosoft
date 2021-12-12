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

package de.bixilon.minosoft.assets.minecraft.index

import de.bixilon.minosoft.assets.minecraft.MinecraftAssetsManager
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileAssetsUtil.toAssetName
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toLong
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPoolRunnable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * Integrated assets-manager, that provides files from the minecraft assets index json
 */
class IndexAssetsManager(
    private val profile: ResourcesProfile,
    private val assetsVersion: String,
    private val indexHash: String,
    private val types: Set<IndexAssetsType>,
) : MinecraftAssetsManager {
    private val verify: Boolean = profile.verify
    private val assets: MutableMap<ResourceLocation, AssetsProperty> = synchronizedMapOf()
    override val namespaces: Set<String> = setOf(ProtocolDefinition.DEFAULT_NAMESPACE)
    override var loaded: Boolean = false
        private set

    private fun downloadAssetsIndex(): Map<String, Any> {
        return Jackson.MAPPER.readValue(FileAssetsUtil.downloadAndGetAsset(Util.formatString(profile.source.mojangPackages,
            mapOf(
                "fullHash" to indexHash,
                "filename" to "$assetsVersion.json",
            ))).second, Jackson.JSON_MAP_TYPE)
    }

    fun verifyAsset(hash: String) {
        val file = File(FileAssetsUtil.getPath(hash))
        if (FileAssetsUtil.verifyAsset(hash, file, verify)) {
            return
        }
        val url = Util.formatString(profile.source.minecraftResources,
            mapOf(
                "hashPrefix" to hash.substring(0, 2),
                "fullHash" to hash,
            ))
        Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloading asset $url" }
        val downloadedHash = FileAssetsUtil.downloadAsset(url)
        if (downloadedHash != hash) {
            throw IOException("Verification of asset $hash failed!")
        }
    }

    override fun load(latch: CountUpAndDownLatch) {
        check(!loaded) { "Already loaded!" }

        var assets = FileUtil.saveReadFile(FileAssetsUtil.getPath(indexHash))?.readJsonObject() ?: downloadAssetsIndex()

        assets["objects"].let { assets = it.asCompound() }
        val tasks = CountUpAndDownLatch(0)
        val assetsLatch = CountUpAndDownLatch(assets.size, parent = latch)

        for ((path, data) in assets) {
            check(data is Map<*, *>)
            val name = path.toAssetName(false)
            if (name == null) {
                assetsLatch.dec()
                continue
            }

            val type = when {
                name.path.startsWith("lang/") -> IndexAssetsType.LANGUAGE
                name.path.startsWith("sounds/") -> IndexAssetsType.SOUNDS
                name.path == "sounds.json" -> IndexAssetsType.SOUNDS
                name.path.startsWith("textures/") -> IndexAssetsType.TEXTURES
                else -> {
                    assetsLatch.dec()
                    continue
                }
            }
            if (type !in this.types) {
                assetsLatch.dec()
                continue
            }

            val size = data["size"]?.toLong() ?: -1
            val hash = data["hash"].toString()
            if (tasks.count > DefaultThreadPool.threadCount - 1) {
                tasks.waitForChange()
            }
            tasks.inc()
            DefaultThreadPool += ThreadPoolRunnable(priority = ThreadPool.LOW) {
                verifyAsset(hash)
                this.assets[name] = AssetsProperty(type, hash, size)
                tasks.dec()
                assetsLatch.dec()
            }
        }
        assetsLatch.await()
        loaded = true
    }

    override fun iterator(): Iterator<Map.Entry<ResourceLocation, ByteArray>> {
        TODO("Not yet implemented")
    }

    override fun unload() {
        assets.clear()
        loaded = false
    }

    override fun get(path: ResourceLocation): InputStream {
        return FileUtil.readFile(FileAssetsUtil.getPath(assets[path]?.hash ?: throw FileNotFoundException("Could not find asset $path")))
    }

    override fun nullGet(path: ResourceLocation): InputStream? {
        return FileUtil.readFile(FileAssetsUtil.getPath(assets[path]?.hash ?: return null))
    }
}
