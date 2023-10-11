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

package de.bixilon.minosoft.assets.file

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.resource.ResourceAssetsManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


object ResourcesAssetsUtil {

    fun create(clazz: Class<*>, canUnload: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX): AssetsManager {
        val rootResources = clazz.classLoader.getResource(prefix) ?: clazz.classLoader.getResource("$prefix/.assets")
        if (rootResources == null) {
            Log.log(LogMessageType.OTHER, LogLevels.FATAL) { "Can not find \"$prefix\" assets root for $clazz" }
            return ResourceAssetsManager(clazz, prefix)
        }

        return when (rootResources.protocol) {
            "file" -> DirectoryAssetsManager(rootResources.path.removeSuffix("/").removeSuffix(prefix), canUnload, prefix) // Read them directly from the folder
            "jar" -> {
                val path: String = rootResources.path
                val jarPath = path.substring(5, path.indexOf("!"))
                val zip = URLDecoder.decode(jarPath, StandardCharsets.UTF_8)
                ZipAssetsManager(zip, canUnload = canUnload, prefix = prefix)
            }

            else -> {
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Can not find resource manager for $rootResources" }

                ResourceAssetsManager(clazz, prefix)
            }
        }
    }
}
