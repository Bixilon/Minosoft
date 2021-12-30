package de.bixilon.minosoft.data.player.properties.textures

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.LongUtil.toLong
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.util.YggdrasilUtil
import de.bixilon.minosoft.util.json.Jackson
import java.util.*

class PlayerTextures(
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
