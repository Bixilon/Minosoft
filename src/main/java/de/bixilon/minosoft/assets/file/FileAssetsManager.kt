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

package de.bixilon.minosoft.assets.file

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream

abstract class FileAssetsManager : AssetsManager {
    override var loaded: Boolean = false
        protected set
    override var image: ByteArray? = null
        protected set
    override var properties: AssetsManagerProperties? = null
        protected set
    protected val assets: MutableMap<ResourceLocation, ByteArray> = mutableMapOf()
    override var namespaces: Set<String> = setOf()
        protected set


    override fun get(path: ResourceLocation): InputStream {
        return ByteArrayInputStream(assets[path] ?: throw FileNotFoundException("Can not find asset $path"))
    }

    override fun nullGet(path: ResourceLocation): InputStream? {
        return ByteArrayInputStream(assets[path] ?: return null)
    }

    override fun unload() {
        assets.clear()
        loaded = false
    }

    override fun iterator(): Iterator<Map.Entry<ResourceLocation, ByteArray>> {
        return assets.iterator()
    }
}
