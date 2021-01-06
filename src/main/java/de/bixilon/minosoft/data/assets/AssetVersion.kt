package de.bixilon.minosoft.data.assets

import de.bixilon.minosoft.data.mappings.versions.Version

data class AssetVersion(
    val version: Version,
    val indexVersion: String?,
    val indexHash: String?,
    val clientJarHash: String?,
    val jarAssetsHash: String?,
    val minosoftMappings: String?
)
