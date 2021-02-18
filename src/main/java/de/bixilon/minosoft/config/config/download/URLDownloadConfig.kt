package de.bixilon.minosoft.config.config.download

data class URLDownloadConfig(
    var resources: String = "https://gitlab.com/Bixilon/minosoft/-/raw/\${branch}/data/resources/\${hashPrefix}/\${fullHash}.tar.gz?inline=false",
)
