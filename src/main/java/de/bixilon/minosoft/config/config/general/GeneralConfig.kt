package de.bixilon.minosoft.config.config.general

import com.squareup.moshi.Json
import de.bixilon.minosoft.config.Configuration
import de.bixilon.minosoft.util.logging.LogLevels

data class GeneralConfig(
    var version: Int = Configuration.LATEST_CONFIG_VERSION,
    @Json(name = "log_level") var logLevel: LogLevels = LogLevels.WARNING,
    var language: String = "en_US",
)
