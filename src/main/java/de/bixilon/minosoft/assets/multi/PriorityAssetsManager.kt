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

package de.bixilon.minosoft.assets.multi

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * A set of assets managers (sorted)
 */
class PriorityAssetsManager(
    vararg managers: AssetsManager,
) : MultiAssetsManager {
    private val managers: MutableSet<AssetsManager> = mutableSetOf()
    override val loaded: Boolean
        get() {
            for (manager in managers) {
                if (!manager.loaded) {
                    return false
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
        for (manager in managers) {
            manager.unload()
        }
        this.managers.clear()
    }

    fun add(manager: AssetsManager) {
        this.managers += manager
    }

    operator fun plusAssign(manager: AssetsManager) = add(manager)

    override fun get(path: ResourceLocation): InputStream {
        return getOrNull(path) ?: throw FileNotFoundException("Can not find asset $path")
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        for (manager in managers) {
            return manager.getOrNull(path) ?: continue
        }
        return null
    }

    override fun getAll(path: ResourceLocation, list: MutableList<InputStream>) {
        for (manager in managers) {
            manager.getAll(path, list)
        }
    }

    override fun load(latch: AbstractLatch?) {
        for (manager in managers) {
            if (manager.loaded) {
                continue
            }
            manager.load(latch)
        }
    }

    override fun getAssetsManager(path: ResourceLocation): AssetsManager? {
        for (manager in managers) {
            return manager.getAssetsManager(path) ?: continue
        }

        return null
    }
}
