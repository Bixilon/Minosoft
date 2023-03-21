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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import java.io.FileInputStream

object MinosoftMeta {
    var root: MetaRoot = unsafeNull()
        private set


    fun load() {
        val file = FileInputStream("/home/moritz/git/gitlab.bixilon.de/bixilon/minosoft-meta/index.json")
        this.root = file.readJson<MetaRoot>()
    }

    private fun MetaVersionEntry.load(type: String): JsonObject {
        val file = FileInputStream("/home/moritz/git/gitlab.bixilon.de/bixilon/minosoft-meta/${this.version}/$type.json")
        return file.readJsonObject()
    }

    fun MetaTypeEntry.load(type: String, version: Version): JsonObject? {
        var previous: MetaVersionEntry? = null
        for (entry in this) {
            if (entry.version == "_") {
                previous = entry
                continue
            }
            val entryVersion = Versions[entry.version] ?: throw IllegalArgumentException("Unknown meta version ${entry.version}")
            if (entryVersion > version) break
            previous = entry
        }
        if (previous == null) return null

        return previous.load(type)
    }
}
