package de.bixilon.minosoft.data.player.properties

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.player.properties.textures.PlayerTextures
import de.bixilon.minosoft.util.KUtil.listCast
import de.bixilon.minosoft.util.KUtil.trim
import de.bixilon.minosoft.util.Util
import java.net.URL
import java.util.*

class PlayerProperties(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val textures: PlayerTextures? = null,
) {
    companion object {
        const val URL = "https://sessionserver.mojang.com/session/minecraft/profile/\${uuid}?unsigned=false"
        const val TEXTURE_PROPERTIES = "textures"


        fun fetch(uuid: UUID): PlayerProperties {
            val url = Util.formatString(URL, mapOf("uuid" to uuid.trim()))
            val data = URL(url).openStream().readJsonObject()

            var textures: PlayerTextures? = null

            data["properties"]?.listCast()?.let {
                for (property in it) {
                    check(property is Map<*, *>)
                    when (val name = property["name"]) {
                        TEXTURE_PROPERTIES -> textures = PlayerTextures.of(property["value"].toString(), property["signature"]?.toString() ?: throw IllegalArgumentException("Texture data must be signed"))
                        else -> throw IllegalArgumentException("Unknown player property $name")
                    }
                }

            }

            return PlayerProperties(
                textures = textures,
            )
        }
    }
}
