package de.bixilon.minosoft.data.player.properties.textures

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.kutil.url.URLUtil.checkWeb
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil
import java.net.URL

open class PlayerTexture(
    val url: URL,
) {
    @JsonIgnore
    var data: ByteArray? = null
        private set

    init {
        url.checkWeb()

        check(urlMatches(url, ALLOWED_DOMAINS) && !urlMatches(url, BLOCKED_DOMAINS)) { "URL hostname is not allowed!" }
    }

    fun read(): ByteArray {
        val sha256 = when (url.host) {
            "textures.minecraft.net" -> url.file.split("/").last()
            else -> TODO("Can not get texture identifier!")
        }

        FileUtil.safeReadFile(FileAssetsUtil.getPath(sha256), true)?.let {
            val data = it.readAllBytes()
            this.data = data
            return data
        }

        val input = url.openStream()
        if (input.available() > MAX_TEXTURE_SIZE) {
            throw IllegalStateException("Texture is too big!")
        }
        val data = FileAssetsUtil.saveAndGet(input)
        return data.second
    }

    companion object {
        private const val MAX_TEXTURE_SIZE = 64 * 64 * 3 + 100 // width * height * rgb + some padding
        private val ALLOWED_DOMAINS = arrayOf(".minecraft.net", ".mojang.com")
        private val BLOCKED_DOMAINS = arrayOf("bugs.mojang.com", "education.minecraft.net", "feedback.minecraft.net")

        private fun urlMatches(url: URL, domains: Array<String>): Boolean {
            for (checkURL in domains) {
                if (url.host.endsWith(checkURL)) {
                    return true
                }
            }
            return false
        }
    }
}
