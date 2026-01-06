/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.resource.ResourceAssetsManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path

object ResourcesAssetsUtil {

    private fun Path.getParent(up: Int): Path {
        var path = this
        for (i in 0 until up) {
            path = path.parent
        }
        return path
    }

    fun create(clazz: Class<*>, canUnload: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX): AssetsManager {
        val root = clazz.classLoader.getResource(prefix) ?: clazz.classLoader.getResource("$prefix/.assets")
        if (root === null) {
            throw FileNotFoundException("Can not find \"$prefix\" assets root for $clazz")
        }

        return when (root.protocol) {
            "file" -> DirectoryAssetsManager(Path.of(root.toURI()).getParent(prefix.count { it == '/' } + 1), canUnload, prefix)
            "jar" -> {
                val path: String = root.path
                val jarPath = path.substring(5, path.indexOf("!"))
                val zip = URLDecoder.decode(jarPath, StandardCharsets.UTF_8)
                ZipAssetsManager(zip, canUnload = canUnload, prefix = prefix)
            }

            else -> {
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Can not find resource manager for $root" }

                ResourceAssetsManager(clazz, prefix)
            }
        }
    }
}
