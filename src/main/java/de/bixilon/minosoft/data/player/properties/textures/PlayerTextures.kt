package de.bixilon.minosoft.data.player.properties.textures

import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.minosoft.util.KUtil.toLong
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.YggdrasilUtil
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import java.util.*

class PlayerTextures(
    val name: String?,
    val uuid: UUID?,
    val date: Date?,
    val skin: SkinPlayerTexture?,
    val cape: PlayerTexture?,
    val elytra: PlayerTexture?,
) {

    companion object {
        fun of(encoded: String, signature: String): PlayerTextures {
            check(YggdrasilUtil.verify(encoded, signature)) { "Texture signature is invalid!" }

            val json: Map<String, Any> = Jackson.MAPPER.readValue(Base64.getDecoder().decode(encoded), Jackson.JSON_MAP_TYPE)

            // Data also contains `signatureRequired`
            val textures = json["textures"]?.compoundCast()
            return PlayerTextures(
                name = json["profileName"]?.toString(),
                uuid = json["profileId"]?.toString()?.let { Util.getUUIDFromString(it) },
                date = json["timestamp"]?.toLong()?.let { Date(it) },
                skin = textures?.get("SKIN")?.compoundCast()?.let { return@let Jackson.MAPPER.convertValue(it) },
                cape = textures?.get("CAPE")?.compoundCast()?.let { return@let Jackson.MAPPER.convertValue(it) },
                elytra = textures?.get("ELYTRA")?.compoundCast()?.let { return@let Jackson.MAPPER.convertValue(it) },
            )
        }
    }
}
