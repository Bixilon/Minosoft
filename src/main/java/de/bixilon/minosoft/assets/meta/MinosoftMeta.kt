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

package de.bixilon.minosoft.assets.meta

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.string.StringUtil.formatPlaceholder
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.mbf.MBFBinaryReader
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.util.FileAssetsTypes
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.assets.util.HashTypes
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.http.DownloadUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.ByteArrayInputStream

object MinosoftMeta {
    private val INDEX = minosoft("mapping/minosoft-meta.json")
    var root: MetaRoot = unsafeNull()
        private set


    fun load() {
        this.root = IntegratedAssets.DEFAULT[INDEX].readJson<MetaRoot>()
    }

    private fun ByteArray.load(): JsonObject {
        return MBFBinaryReader(ByteArrayInputStream(this)).readMBF().data.unsafeCast()
    }

    private fun verify(url: String, hash: String): JsonObject {
        val url = url.formatPlaceholder(
            "hashPrefix" to hash.substring(0, 2),
            "fullHash" to hash,
        ).toURL()
        Log.log(LogMessageType.ASSETS, LogLevels.VERBOSE) { "Downloading minosoft meta $url" }
        val data = FileAssetsUtil.read(url.openStream(), type = FileAssetsTypes.META, compress = false, hash = HashTypes.SHA256)

        if (data.hash != hash) {
            throw IllegalStateException("Minosoft meta data mismatch (expected=$hash, hash=${data.hash}!")
        }

        return data.data.load()
    }

    private fun MetaVersionEntry.load(profile: ResourcesProfile): JsonObject {
        FileAssetsUtil.readOrNull(this.hash, FileAssetsTypes.META, compress = false)?.let { return it.load() }

        return DownloadUtil.retry(profile.source.minosoftMeta) { verify(it, hash) }
    }

    fun MetaTypeEntry.load(profile: ResourcesProfile, version: Version): JsonObject? {
        var previous: MetaVersionEntry? = null
        var previousVersion: Version? = null

        for (entry in this) {
            if (entry.version == "_") {
                if (previous != null) continue
                previous = entry
                continue
            }
            val entryVersion = Versions[entry.version] ?: throw IllegalArgumentException("Unknown meta version ${entry.version}")
            if (entryVersion > version) continue

            if (previousVersion != null && previousVersion > entryVersion) {
                continue
            }
            previousVersion = entryVersion
            previous = entry
        }
        if (previous == null) return null

        return previous.load(profile)
    }
}
