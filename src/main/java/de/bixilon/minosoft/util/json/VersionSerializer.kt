/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.json

import com.squareup.moshi.*
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions

object VersionSerializer : JsonAdapter<Version>() {
    @FromJson
    override fun fromJson(jsonReader: JsonReader): Version? {
        return when (jsonReader.peek()) {
            JsonReader.Token.NULL -> null
            JsonReader.Token.NUMBER -> Versions.getVersionById(jsonReader.nextInt())
            JsonReader.Token.STRING -> Versions.getVersionByName(jsonReader.nextString())
            else -> TODO()
        }
    }

    @ToJson
    override fun toJson(jsonWriter: JsonWriter, version: Version?) {
        if (version == null) {
            jsonWriter.nullValue()
            return
        }
        jsonWriter.value(version.versionId)
    }
}
