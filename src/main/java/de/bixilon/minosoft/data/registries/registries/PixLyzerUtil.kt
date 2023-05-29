/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.registries

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.HashTypes
import de.bixilon.minosoft.assets.util.InputStreamUtil.readMBFMap
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.protocol.versions.Version
import java.io.ByteArrayInputStream

object PixLyzerUtil {

    private fun ByteArray.read(): JsonObject {
        return ByteArrayInputStream(this).readMBFMap().toJsonObject() ?: throw IllegalStateException("Could not read pixlyzer data!")
    }

    private fun verify(url: String, hash: String): Map<String, Any> {
        FileAssetsUtil.readOrNull(hash, type = FileAssetsTypes.PIXLYZER, compress = false)?.let { return it.read() }


        val data = FileAssetsUtil.read(url.formatPlaceholder(
            "hashPrefix" to hash.substring(0, 2),
            "fullHash" to hash,
        ).toURL().openStream(), type = FileAssetsTypes.PIXLYZER, compress = false, hash = HashTypes.SHA1)

        if (data.hash != hash) {
            throw IllegalStateException("Pixlyzer data mismatch (expected=$hash, hash=${data.hash}!")
        }

        return data.data.read()
    }

    fun loadPixlyzerData(version: Version, profile: ResourcesProfile): JsonObject {
        val pixlyzerHash = AssetsVersionProperties[version]?.pixlyzerHash ?: throw IllegalStateException("$version has no pixlyzer data available!")

        return verify(profile.source.pixlyzer, pixlyzerHash)
    }

    fun loadRegistry(version: Version, profile: ResourcesProfile, latch: AbstractLatch): Registries {
        val registries = Registries()
        val data = loadPixlyzerData(version, profile)

        registries.load(version, data, latch)

        return registries
    }
}
