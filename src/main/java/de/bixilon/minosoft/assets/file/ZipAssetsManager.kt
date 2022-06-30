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

package de.bixilon.minosoft.assets.file

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.FileAssetsUtil.toAssetName
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

/**
 * Assets-manager that just reads a zip file and provides all files in it
 * Probably a resource pack
 */
class ZipAssetsManager(
    private val inputStream: ZipInputStream,
    canUnload: Boolean = true,
    val prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX,
) : FileAssetsManager(canUnload) {

    constructor(file: File, canUnload: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX) : this(ZipInputStream(FileInputStream(file)), canUnload, prefix)
    constructor(path: String, canUnload: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX) : this(File(path), canUnload, prefix)

    override fun load(latch: CountUpAndDownLatch) {
        check(!loaded) { "Already loaded!" }

        val namespaces: MutableSet<String> = mutableSetOf()
        while (true) {
            val entry = inputStream.nextEntry ?: break
            if (entry.isDirectory) {
                continue
            }
            when (val name = entry.name) {
                "pack.png" -> image = inputStream.readAllBytes()
                "pack.mcmeta" -> properties = inputStream.readJson(false)
                else -> {
                    val resourceLocation = name.toAssetName(prefix = prefix) ?: continue
                    namespaces += resourceLocation.namespace
                    assets[resourceLocation] = inputStream.readAllBytes()
                }
            }
        }

        inputStream.close()
        this.namespaces = namespaces
        loaded = true
    }

    override fun contains(path: ResourceLocation): Boolean {
        return path in assets
    }
}
