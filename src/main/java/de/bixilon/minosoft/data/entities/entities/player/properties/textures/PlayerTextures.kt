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

package de.bixilon.minosoft.data.entities.entities.player.properties.textures

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.LongUtil.toLong
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.util.YggdrasilUtil
import de.bixilon.minosoft.util.json.Jackson
import java.util.*

data class PlayerTextures(
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val name: String?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val uuid: UUID?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val date: Date?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val skin: SkinPlayerTexture?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val cape: PlayerTexture?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val elytra: PlayerTexture?,
) {

    companion object {

        fun of(encoded: String, signature: String): PlayerTextures {
            check(YggdrasilUtil.verify(encoded, signature)) { "Texture signature is invalid!" }

            val json: Map<String, Any> = Jackson.MAPPER.readValue(Base64.getDecoder().decode(encoded), Jackson.JSON_MAP_TYPE)

            // Data also contains `signatureRequired`
            val textures = json["textures"]?.toJsonObject()
            return PlayerTextures(
                name = json["profileName"]?.toString(),
                uuid = json["profileId"]?.toString()?.toUUID(),
                date = json["timestamp"]?.toLong()?.let { Date(it) },
                skin = textures?.get("SKIN")?.toJsonObject()?.let { return@let Jackson.MAPPER.convertValue(it) },
                cape = textures?.get("CAPE")?.toJsonObject()?.let { return@let Jackson.MAPPER.convertValue(it) },
                elytra = textures?.get("ELYTRA")?.toJsonObject()?.let { return@let Jackson.MAPPER.convertValue(it) },
            )
        }
    }
}
