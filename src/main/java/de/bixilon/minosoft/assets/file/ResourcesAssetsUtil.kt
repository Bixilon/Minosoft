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

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import java.io.FileNotFoundException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


object ResourcesAssetsUtil {

    fun create(clazz: Class<*>, canUnload: Boolean = true, prefix: String = AssetsManager.DEFAULT_ASSETS_PREFIX): AssetsManager {
        val rootResources = clazz.classLoader.getResource(prefix) ?: throw FileNotFoundException("Can not find assets folder in $clazz")

        return when (rootResources.protocol) {
            "file" -> DirectoryAssetsManager(rootResources.path, canUnload, prefix) // Read them directly from the folder
            "jar" -> {
                val path: String = rootResources.path
                val jarPath = path.substring(5, path.indexOf("!"))
                val zip = URLDecoder.decode(jarPath, StandardCharsets.UTF_8)
                ZipAssetsManager(zip, canUnload = canUnload, prefix = prefix)
            }
            else -> throw IllegalStateException("Can not read resources: $rootResources")
        }
    }
}
