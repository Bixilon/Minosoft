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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.connection.ConnectionAssetsManager
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat.packFormat
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsManager
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperty
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

    private fun ConnectionAssetsManager.addPacks(profile: ResourcesProfile, latch: AbstractLatch) {
        for (resourcePack in profile.assets.resourcePacks.reversed()) {
            val manager = resourcePack.type.creator.invoke(resourcePack)
            manager.load(latch)
            this += manager
        }
    }

    fun create(profile: ResourcesProfile, version: Version, latch: AbstractLatch, property: AssetsVersionProperty = AssetsVersionProperties[version] ?: throw IllegalAccessException("$version has no assets!")): ConnectionAssetsManager {
        val properties = profile.createPackProperties(version)

        val assetsManager = ConnectionAssetsManager(properties)

        assetsManager += IntegratedAssets.OVERRIDE

        assetsManager.addPacks(profile, latch)

        if (!profile.assets.disableIndexAssets) {
            assetsManager += IndexAssetsManager(profile, property.indexVersion, property.indexHash, profile.assets.indexAssetsTypes.toSet(), version.packFormat)
        }
        if (!profile.assets.disableJarAssets) {
            assetsManager += JarAssetsManager(property.jarAssetsHash, property.clientJarHash, profile, version, property.jarAssetsTarBytes ?: JarAssetsManager.DEFAULT_TAR_BYTES)
        }
        assetsManager += IntegratedAssets.DEFAULT

        return assetsManager
    }
}
