/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.assets

import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat.packFormat
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsManager
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperty
import de.bixilon.minosoft.assets.session.SessionAssetsManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.protocol.versions.Version

object AssetsLoader {

    private fun ResourcesProfile.createPackProperties(version: Version): AssetsManagerProperties {
        var packFormat = assets.packFormat
        if (packFormat < 0) {
            packFormat = version.packFormat
        }

        return AssetsManagerProperties(PackProperties(packFormat))
    }

    private fun SessionAssetsManager.addResourcePacks(profile: ResourcesProfile) {
        for (pack in profile.assets.resourcePacks.reversed()) {
            val manager = pack.type.creator.invoke(pack)
            this += manager
        }
    }

    fun create(profile: ResourcesProfile, version: Version, property: AssetsVersionProperty = AssetsVersionProperties[version] ?: throw IllegalAccessException("$version has no assets!")): SessionAssetsManager {
        val properties = profile.createPackProperties(version)

        val manager = SessionAssetsManager(properties)

        manager += IntegratedAssets.OVERRIDE

        manager.addResourcePacks(profile)

        for (format in 1..properties.pack.format) {
            manager += IntegratedAssets.VERSIONED[format - 1] ?: continue
        }

        if (!profile.assets.disableIndexAssets) {
            manager += IndexAssetsManager(profile, property.indexVersion, property.indexHash, profile.assets.indexAssetsTypes.toSet(), version.packFormat)
        }
        if (!profile.assets.disableJarAssets) {
            manager += JarAssetsManager(property.jarAssetsHash, property.clientJarHash, profile, version, property.jarAssetsTarBytes ?: JarAssetsManager.DEFAULT_TAR_BYTES)
        }
        manager += IntegratedAssets.DEFAULT

        return manager
    }
}
