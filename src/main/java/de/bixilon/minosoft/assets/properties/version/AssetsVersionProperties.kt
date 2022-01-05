/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

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
            PROPERTIES[Versions[versionName] ?: continue] = property
        }
    }

    operator fun get(version: Version): AssetsVersionProperty? {
        return PROPERTIES[version]
    }
}
