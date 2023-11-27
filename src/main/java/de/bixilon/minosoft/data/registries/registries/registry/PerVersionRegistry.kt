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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import java.util.*

class PerVersionRegistry<E, R : AbstractRegistry<E>>(private val registryCreator: () -> R) {
    private lateinit var versions: Map<Int, R>

    fun forVersion(version: Version): R {
        // must loop from the highest version to lowest!
        for ((versionId, registry) in versions) {
            if (version.versionId < versionId) {
                continue
            }
            return registry
        }
        throw IllegalArgumentException("Can not find a registry for version $version")
    }

    fun initialize(data: Map<String, Any>, codec: IdentifierCodec<E>?) {
        check(!this::versions.isInitialized) { "Already initialized!" }

        val versions: SortedMap<Int, R> = sortedMapOf({ t, t2 -> t2 - t })
        for ((versionId, json) in data) {
            val versionId = versionId.toInt()
            val registry = registryCreator()
            registry.update(json.toJsonObject(), Versions.getById(versionId)!!, null)
            versions[versionId] = registry
        }
        this.versions = versions
    }
}
