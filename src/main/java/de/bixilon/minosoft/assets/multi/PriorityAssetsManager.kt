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

package de.bixilon.minosoft.assets.multi

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.CountUpAndDownLatch
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * A set of assets managers (sorted)
 */
class PriorityAssetsManager(
    vararg managers: AssetsManager,
) : MultiAssetsManager {
    private val managers: MutableMap<String, MutableSet<AssetsManager>> = mutableMapOf()
    override val namespaces: MutableSet<String> = mutableSetOf()
    override val loaded: Boolean
        get() {
            for (managers in managers.values) {
                for (manager in managers) {
                    if (!manager.loaded) {
                        return false
                    }
                }
            }
            return true
        }

    init {
        for (manager in managers) {
            add(manager)
        }
    }

    override fun unload() {
        for ((_, managers) in managers) {
            for (manager in managers) {
                manager.unload()
            }
        }
        this.managers.clear()
    }

    fun add(manager: AssetsManager) {
        for (namespace in manager.namespaces) {
            this.managers.getOrPut(namespace) { mutableSetOf() } += manager
            this.namespaces += namespace
        }
    }

    operator fun plusAssign(manager: AssetsManager) = add(manager)

    override fun get(path: ResourceLocation): InputStream {
        return nullGet(path) ?: throw FileNotFoundException("Can not find assets-manager for $path")
    }

    override fun nullGet(path: ResourceLocation): InputStream? {
        val managers = this.managers[path.namespace] ?: return null
        for (manager in managers) {
            return manager.nullGet(path) ?: continue
        }
        return null
    }

    override fun load(latch: CountUpAndDownLatch) {
        for ((_, managers) in managers) {
            for (manager in managers) {
                if (manager.loaded) {
                    continue
                }
                manager.load(latch)
            }
        }
    }

    override fun iterator(): Iterator<Map.Entry<ResourceLocation, ByteArray>> {
        TODO("Not yet implemented")
    }
}
