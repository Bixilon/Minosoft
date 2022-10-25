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

package de.bixilon.minosoft.assets.directory

import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.util.FileAssetsUtil.toAssetName
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * Provides assets that are saved in a directory (on your hard drive)
 */

class DirectoryAssetsManager(
    private val rootPath: String,
    private val canUnload: Boolean = true,
    val prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX,
) : AssetsManager {
    private val basePath = File(rootPath).slashPath + "/" + prefix
    override val namespaces: MutableSet<String> = mutableSetOf()
    private var assets: MutableSet<ResourceLocation> = mutableSetOf()
    override var loaded: Boolean = false
        private set
    override var image: ByteArray? = null
        private set
    override var properties: AssetsManagerProperties? = null
        private set

    private val ResourceLocation.filePath: String
        get() = "$basePath/$namespace/$path"

    private fun scanDirectory(directory: File) {
        for (file in directory.listFiles() ?: return) {
            if (file.isDirectory) {
                scanDirectory(file)
                continue
            }
            val path = file.slashPath.removePrefix(basePath).removePrefix("/").toAssetName(false, prefix) ?: continue
            assets += path
            namespaces += path.namespace
        }
    }

    override fun load(latch: CountUpAndDownLatch) {
        check(!loaded) { "Already loaded!" }
        scanDirectory(File(basePath))
        File("$rootPath/pack.png").let { if (it.exists() && it.isFile) image = FileInputStream(it).readAllBytes() }
        File("$rootPath/pack.mcmeta").let { if (it.exists() && it.isFile) properties = FileInputStream(it).readJson() }
        loaded = true
    }

    override fun unload() {
        if (!canUnload) {
            return
        }
        assets.clear()
        loaded = false
    }

    override fun get(path: ResourceLocation): InputStream {
        if (path !in assets) {
            throw FileNotFoundException("Can not find asset $path")
        }
        return FileUtil.readFile(path.filePath, false)
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        if (path !in assets) {
            return null
        }
        return FileUtil.safeReadFile(path.filePath, false)
    }

    override fun contains(path: ResourceLocation): Boolean {
        return path in assets
    }
}
