package de.bixilon.minosoft.gui.eros.main.play.server.card

import com.github.luben.zstd.ZstdOutputStream
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.util.Util
import javafx.scene.image.Image
import java.io.File
import java.io.FileOutputStream

object FaviconManager {

    val Server.favicon: Image?
        get() {
            val hash = this.faviconHash ?: return null
            try {
                return Image(FileUtil.readFile(FileAssetsUtil.getPath(hash)))
            } catch (exception: Throwable) {
                this.faviconHash = null
            }
            return null
        }

    fun Server.saveFavicon(favicon: ByteArray?, faviconHash: String = Util.sha256(favicon)) {
        if (this.faviconHash == faviconHash) {
            return
        }
        this.faviconHash = faviconHash
        val file = File(FileAssetsUtil.getPath(faviconHash))
        if (file.exists()) {
            return
        }
        if (favicon == null) {
            file.delete() // ToDo: Check if other servers are using it
            return
        }
        val outputStream = ZstdOutputStream(FileOutputStream(file))
        outputStream.write(favicon)
        outputStream.close()

    }
}
