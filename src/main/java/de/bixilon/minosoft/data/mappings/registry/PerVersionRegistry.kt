/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.registry

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.versions.Version
import java.util.*

class PerVersionRegistry<T : RegistryItem> {
    private lateinit var versions: Map<Int, Registry<T>>

    fun forVersion(version: Version): Registry<T> {
        // must loop from highest version to lowest!
        for ((versionId, registry) in versions) {
            if (version.versionId < versionId) {
                continue
            }
            return registry
        }
        throw IllegalArgumentException("Can not find a registry for version $version")
    }

    fun initialize(data: JsonObject, deserializer: ResourceLocationDeserializer<T>) {
        check(!this::versions.isInitialized) { "Already initialized!" }

        val versions: SortedMap<Int, Registry<T>> = sortedMapOf({ t, t2 -> t2 - t })
        for ((versionId, json) in data.entrySet()) {
            versions[Integer.parseInt(versionId)] = Registry<T>().initialize(json as JsonObject, null, deserializer)
        }
        this.versions = versions.toMap()
    }
}
