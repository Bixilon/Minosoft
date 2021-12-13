package de.bixilon.minosoft.data.player.properties.textures

import de.bixilon.minosoft.util.KUtil.check
import java.net.URL

open class PlayerTexture(
    val url: URL,
) {
    init {
        url.check()

        check(urlMatches(url, ALLOWED_DOMAINS) && !urlMatches(url, BLOCKED_DOMAINS)) { "URL hostname is not allowed!" }
    }


    companion object {
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
