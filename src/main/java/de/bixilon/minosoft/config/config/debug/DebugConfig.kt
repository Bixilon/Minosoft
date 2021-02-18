package de.bixilon.minosoft.config.config.debug

import com.squareup.moshi.Json

data class DebugConfig(
    @Json(name = "verify_assets") val verifyAssets: Boolean = true,
)
