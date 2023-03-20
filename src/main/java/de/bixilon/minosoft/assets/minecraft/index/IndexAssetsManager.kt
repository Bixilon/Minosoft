/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.primitive.LongUtil.toLong
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.assets.error.AssetCorruptedError
import de.bixilon.minosoft.assets.error.AssetNotFoundError
import de.bixilon.minosoft.assets.minecraft.MinecraftAssetsManager
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileAssetsUtil.toAssetName
import de.bixilon.minosoft.assets.util.HashTypes
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.io.ByteArrayInputStream
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
    override val namespaces: Set<String> = setOf(Namespaces.MINECRAFT)
    override var loaded: Boolean = false
        private set

    private fun readAssetsIndex(): Map<String, Any> {
        return FileAssetsUtil.readOrNull(indexHash, FileAssetsTypes.GAME, verify = verify)?.let { ByteArrayInputStream(it).readJsonObject() } ?: downloadAssetsIndex()
    }

    private fun downloadAssetsIndex(): Map<String, Any> {
        Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloading assets index ($indexHash)" }
        val data = FileAssetsUtil.read(profile.source.mojangPackages.formatPlaceholder(
            "fullHash" to indexHash,
            "filename" to "$assetsVersion.json",
        ).toURL().openStream(), FileAssetsTypes.GAME, hash = HashTypes.SHA1).data

        return Jackson.MAPPER.readValue(data, Jackson.JSON_MAP_TYPE)
    }

    fun verifyAsset(property: AssetsProperty) {
        if (FileAssetsUtil.verify(property.hash, type = property.type.type, lazy = !verify)) {
            return
        }
        val url = profile.source.minecraftResources.formatPlaceholder(
            "hashPrefix" to property.hash.substring(0, 2),
            "fullHash" to property.hash,
        ).toURL()

        Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloading asset $url" }

        val hash = FileAssetsUtil.save(url.openStream(), type = property.type.type, hash = HashTypes.SHA1)
        if (hash != property.hash) {
            throw IOException("Verification of asset failed (expected=${property.hash}, hash=$hash)!")
        }
    }

    override fun load(latch: CountUpAndDownLatch) {
        check(!loaded) { "Already loaded!" }

        var assets = readAssetsIndex()

        assets["objects"].let { assets = it.asJsonObject() }

        val worker = UnconditionalWorker()

        val hashes: MutableSet<String> = ObjectOpenHashSet()

        for ((path, data) in assets) {
            check(data is Map<*, *>)
            val name = path.toAssetName(false) ?: continue

            val type = IndexAssetsType.determinate(name)
            if (type == null || type !in this.types) {
                continue
            }

            val size = data["size"]?.toLong() ?: -1
            val hash = data["hash"].toString()
            val property = AssetsProperty(type, hash, size)

            this.assets[name] = property

            if (hash in hashes) {
                // sound is used multiple times
                continue
            }
            hashes += hash

            worker += UnconditionalTask(priority = ThreadPool.LOW) {
                verifyAsset(property) // TODO: This first verifies the asset, it will be verified another time when loading
            }
        }

        worker.work(latch)
        loaded = true
    }

    override fun unload() {
        assets.clear()
        loaded = false
    }

    override fun get(path: ResourceLocation): InputStream {
        val property = assets[path] ?: throw AssetNotFoundError(path)
        return FileAssetsUtil.readOrNull(property.hash, type = property.type.type, verify = verify)?.let { ByteArrayInputStream(it) } ?: throw AssetCorruptedError(path)
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        val property = assets[path] ?: return null
        return FileAssetsUtil.readOrNull(property.hash, type = property.type.type, verify = verify)?.let { ByteArrayInputStream(it) }
    }

    override fun contains(path: ResourceLocation): Boolean {
        return path in assets
    }
}
