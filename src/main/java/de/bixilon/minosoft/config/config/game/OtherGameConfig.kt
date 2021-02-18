package de.bixilon.minosoft.config.config.game

import com.squareup.moshi.Json

data class OtherGameConfig(
    @Json(name = "anti_moire_pattern") var antiMoirePattern: Boolean = true,
)
