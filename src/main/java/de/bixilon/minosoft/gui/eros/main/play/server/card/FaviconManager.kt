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

package de.bixilon.minosoft.gui.eros.main.play.server.card

import com.github.luben.zstd.ZstdOutputStream
import de.bixilon.kutil.hash.HashUtil.sha256
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.assets.util.FileUtil.mkdirParent
import de.bixilon.minosoft.assets.util.PathUtil
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import javafx.scene.image.Image
import java.io.FileOutputStream

object FaviconManager {

    val AbstractServer.favicon: Image?
        get() {
            val hash = this.faviconHash ?: return null
            try {
                return Image(FileUtil.readFile(PathUtil.getAssetsPath(hash = hash, type = FileAssetsTypes.FAVICON)))
            } catch (exception: Throwable) {
                this.faviconHash = null
            }
            return null
        }

    fun AbstractServer.saveFavicon(favicon: ByteArray?, faviconHash: String = favicon!!.sha256()) {
        if (this.faviconHash == faviconHash) {
            return
        }
        this.faviconHash = faviconHash
        val file = PathUtil.getAssetsPath(hash = faviconHash, type = FileAssetsTypes.FAVICON).toFile()
        if (file.exists()) {
            return
        }
        if (favicon == null) {
            file.delete() // ToDo: Check if other servers are using it
            return
        }
        file.mkdirParent()
        val outputStream = ZstdOutputStream(FileOutputStream(file), 5)
        outputStream.write(favicon)
        outputStream.close()
    }
}
