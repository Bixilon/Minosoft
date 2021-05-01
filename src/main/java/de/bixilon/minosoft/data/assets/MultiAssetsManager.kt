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

import de.bixilon.minosoft.data.mappings.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL

class MultiAssetsManager(
    vararg assetsManagers: AssetsManager,
) : AssetsManager {
    private val assetsManagers: MutableMap<String, MutableList<AssetsManager>> = mutableMapOf()

    override val namespaces: Set<String>
        get() = assetsManagers.keys

    init {
        for (assetsManager in assetsManagers) {
            addAssetManager(assetsManager)
        }
    }

    fun addAssetManager(assetsManager: AssetsManager) {
        if (assetsManager === this) {
            throw IllegalArgumentException("Can not add ourself!")
        }
        for (namespace in assetsManager.namespaces) {
            this.assetsManagers.getOrPut(namespace) { mutableListOf() }.add(assetsManager)
        }
    }

    private fun <T> runInAssetManagers(resourceLocation: ResourceLocation, runnable: (assetManager: AssetsManager) -> T): T {
        val assetsManagers = this.assetsManagers[resourceLocation.namespace] ?: throw FileNotFoundException("Can not find a asset manager for $resourceLocation")

        for (assetManager in assetsManagers) {
            try {
                return runnable(assetManager)
            } catch (ignored: FileNotFoundException) {
                continue
            }
        }

        throw FileNotFoundException("Can not find a asset manager that provides $resourceLocation")
    }

    override fun getAssetURL(resourceLocation: ResourceLocation): URL {
        return runInAssetManagers(resourceLocation) { it.getAssetURL(resourceLocation) }
    }

    override fun getAssetSize(resourceLocation: ResourceLocation): Long {
        return runInAssetManagers(resourceLocation) { it.getAssetSize(resourceLocation) }
    }

    override fun readAssetAsStream(resourceLocation: ResourceLocation): InputStream {
        return runInAssetManagers(resourceLocation) { it.readAssetAsStream(resourceLocation) }
    }
}
