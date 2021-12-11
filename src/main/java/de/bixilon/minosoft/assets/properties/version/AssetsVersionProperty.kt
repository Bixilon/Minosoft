package de.bixilon.minosoft.assets.properties.version

data class AssetsVersionProperty(
    val indexVersion: String,
    val indexHash: String,
    val clientJarHash: String,
    val jarAssetsHash: String,
    val pixlyzerHash: String,
)
