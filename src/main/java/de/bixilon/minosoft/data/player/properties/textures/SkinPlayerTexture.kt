package de.bixilon.minosoft.data.player.properties.textures

import de.bixilon.minosoft.data.player.properties.textures.metadata.SkinMetadata
import java.net.URL

class SkinPlayerTexture(
    url: URL,
    val metadata: SkinMetadata = SkinMetadata(),
) : PlayerTexture(url = url)
