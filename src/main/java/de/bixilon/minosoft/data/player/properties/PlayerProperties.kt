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

package de.bixilon.minosoft.data.player.properties

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.uuid.UUIDUtil.trim
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.player.properties.textures.PlayerTextures
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.URL
import java.util.*

data class PlayerProperties(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val textures: PlayerTextures? = null,
) {
    companion object {
        const val URL = "https://sessionserver.mojang.com/session/minecraft/profile/\${uuid}?unsigned=false"
        const val TEXTURE_PROPERTIES = "textures"


        fun fetch(uuid: UUID): PlayerProperties {
            val url = URL.formatPlaceholder("uuid" to uuid.trim())
            val data = URL(url).openStream().readJsonObject()

            var textures: PlayerTextures? = null

            data["properties"]?.toJsonList()?.let {
                for (property in it) {
                    check(property is Map<*, *>)
                    when (val name = property["name"]) {
                        TEXTURE_PROPERTIES -> textures = PlayerTextures.of(property["value"].toString(), property["signature"]?.toString() ?: throw IllegalArgumentException("Texture data must be signed"))
                        else -> Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Unknown player property $name: ${property["value"].toString()}" }
                    }
                }

            }

            return PlayerProperties(
                textures = textures,
            )
        }
    }
}
