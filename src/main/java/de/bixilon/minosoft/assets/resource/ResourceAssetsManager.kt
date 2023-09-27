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

package de.bixilon.minosoft.assets.resource

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException
import java.io.InputStream

@Deprecated("Super slow")
class ResourceAssetsManager(
    val clazz: Class<*>,
    val prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX,
) : AssetsManager {
    override var loaded: Boolean = false

    override fun load(latch: AbstractLatch?) {
        Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Loaded resource assets manager for class $clazz. Performance will be impacted!" }
    }

    override fun unload() = Unit

    private fun open(path: ResourceLocation): InputStream? {
        val url = "/$prefix/${path.namespace}/${path.path}"

        return clazz.getResourceAsStream(url)
    }

    override fun getAssetsManager(path: ResourceLocation): AssetsManager? {
        val stream = open(path) ?: return null
        stream.close()
        return this
    }

    override fun get(path: ResourceLocation): InputStream {
        return open(path) ?: throw FileNotFoundException("Can not find asset $path")
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        return open(path)
    }
}
