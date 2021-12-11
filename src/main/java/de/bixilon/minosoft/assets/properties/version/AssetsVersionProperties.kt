package de.bixilon.minosoft.assets.properties.version

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object AssetsVersionProperties {
    private val ASSETS_PROPERTIES_FILE = "minosoft:mapping/assets_properties.json".toResourceLocation()
    private val PROPERTIES: MutableMap<Version, AssetsVersionProperty> = mutableMapOf()

    fun load() {
        if (PROPERTIES.isNotEmpty()) {
            throw IllegalStateException("Already loaded!")
        }
        val assetsProperties: Map<String, AssetsVersionProperty> = Minosoft.MINOSOFT_ASSETS_MANAGER[ASSETS_PROPERTIES_FILE].readJson()
        for ((versionName, property) in assetsProperties) {
            PROPERTIES[Versions.getVersionByName(versionName) ?: continue] = property
        }
    }

    operator fun get(version: Version): AssetsVersionProperty? {
        return PROPERTIES[version]
    }
}
