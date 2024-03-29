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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class MemoryAssetsManager : AssetsManager {
    private val entries: MutableMap<ResourceLocation, ByteArray> = mutableMapOf()
    override val loaded: Boolean = true

    override fun get(path: ResourceLocation): InputStream {
        return ByteArrayInputStream(entries[path] ?: throw FileNotFoundException(path.toString()))
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        return entries[path]?.let { ByteArrayInputStream(it) }
    }

    override fun load(latch: AbstractLatch?) {
    }

    override fun unload() {
    }

    override fun getAssetsManager(path: ResourceLocation): AssetsManager? {
        return if (path in entries) this else null
    }

    fun push(path: ResourceLocation, data: ByteArray) {
        entries[path] = data
    }

    fun push(path: ResourceLocation, data: String) {
        push(path, data.encodeNetwork())
    }
}
