package de.bixilon.minosoft.data.player.properties.textures.metadata

import com.fasterxml.jackson.annotation.JsonInclude

data class SkinMetadata(
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val model: SkinModel = SkinModel.NORMAL,
)
