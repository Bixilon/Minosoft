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

import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.CountUpAndDownLatch
import java.io.InputStream


/**
 * Provides assets that are saved in a directory (on your hard drive)
 */

class DirectoryAssetsManager(
    private val basePath: String,
) : FileAssetsManager() {

    private val ResourceLocation.filePath: String
        get() = "$basePath/$namespace/$path"

    override fun load(latch: CountUpAndDownLatch) = Unit

    override fun get(path: ResourceLocation): InputStream {
        return FileUtil.readFile(path.filePath, false)
    }

    override fun nullGet(path: ResourceLocation): InputStream? {
        return FileUtil.saveReadFile(path.filePath, false)
    }
}
