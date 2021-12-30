package de.bixilon.minosoft.data.registries.registries

import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.string.StringUtil.format
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.FileUtil
import de.bixilon.minosoft.assets.util.FileUtil.readMBFMap
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.versions.Version
import java.io.ByteArrayInputStream
import java.io.File

object RegistriesLoader {

    fun load(profile: ResourcesProfile, version: Version): Registries {
        // ToDo: Pre flattening support
        val pixlyzerHash = AssetsVersionProperties[version]?.pixlyzerHash ?: throw IllegalStateException("$version has no pixlyzer data available!")

        val pixlyzerData = getPixlyzerData(profile.source.pixlyzer, pixlyzerHash)

        val registries = Registries()
        registries.load(version, pixlyzerData)

        return registries
    }

    private fun getPixlyzerData(url: String, hash: String): Map<String, Any> {
        val path = FileAssetsUtil.getPath(hash)
        val file = File(path)
        if (file.exists()) {
            // ToDo: Verify
            return FileUtil.readFile(file, false).readMBFMap().toJsonObject() ?: throw IllegalStateException("Could not read pixlyzer data!")
        }

        val savedHash = FileAssetsUtil.downloadAndGetAsset(url.format(mapOf(
            "hashPrefix" to hash.substring(0, 2),
            "fullHash" to hash,
        )
        ), false, hashType = FileAssetsUtil.HashTypes.SHA1)
        if (savedHash.first != hash) {
            throw IllegalStateException("Data mismatch, expected $hash, got ${savedHash.first}")
        }

        return ByteArrayInputStream(savedHash.second).readMBFMap().toJsonObject() ?: throw IllegalStateException("Invalid pixlyzer data!")
    }
}
