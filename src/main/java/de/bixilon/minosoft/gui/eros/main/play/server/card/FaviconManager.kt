package de.bixilon.minosoft.gui.eros.main.play.server.card

import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.assets.AssetsUtil
import de.bixilon.minosoft.util.Util
import javafx.scene.image.Image
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object FaviconManager {

    val Server.favicon: Image?
        get() {
            val hash = this.faviconHash ?: return null
            try {
                val path = AssetsUtil.getAssetDiskPath(hash, true)
                return Image(GZIPInputStream(FileInputStream(path)))
            } catch (exception: Throwable) {
                this.faviconHash = null
            }
            return null
        }

    fun Server.saveFavicon(favicon: ByteArray?, faviconHash: String = Util.sha1(favicon)) {
        if (this.faviconHash == faviconHash) {
            return
        }
        this.faviconHash = faviconHash
        val file = File(AssetsUtil.getAssetDiskPath(faviconHash, true))
        if (file.exists()) {
            return
        }
        if (favicon == null) {
            file.delete() // ToDo: Check if other servers are using it
            return
        }
        val outputStream = GZIPOutputStream(FileOutputStream(file))
        outputStream.write(favicon)
        outputStream.close()

    }
}
