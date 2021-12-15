package de.bixilon.minosoft.data.player.properties.textures

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.minosoft.data.player.properties.textures.metadata.SkinMetadata
import java.net.URL

class SkinPlayerTexture(
    url: URL,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val metadata: SkinMetadata = SkinMetadata(),
) : PlayerTexture(url = url)
