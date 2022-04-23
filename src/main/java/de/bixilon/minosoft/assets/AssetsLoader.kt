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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsManager
import de.bixilon.minosoft.assets.multi.PriorityAssetsManager
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperty
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.versions.Version

object AssetsLoader {

    fun create(profile: ResourcesProfile, version: Version, latch: CountUpAndDownLatch, property: AssetsVersionProperty = AssetsVersionProperties[version] ?: throw IllegalAccessException("$version has no assets!")): AssetsManager {
        val assetsManager = PriorityAssetsManager()

        assetsManager += Minosoft.OVERWRITE_ASSETS_MANAGER
        for (resourcePack in profile.assets.resourcePacks.reversed()) {
            resourcePack.type.creator(resourcePack).let {
                it.load(latch)
                assetsManager += it
            }
        }

        if (!profile.assets.disableIndexAssets) {
            assetsManager += IndexAssetsManager(profile, property.indexVersion, property.indexHash, profile.assets.indexAssetsTypes.toSet())
        }
        if (!profile.assets.disableJarAssets) {
            assetsManager += JarAssetsManager(property.jarAssetsHash, property.clientJarHash, profile, version)
        }
        assetsManager += Minosoft.MINOSOFT_ASSETS_MANAGER

        return assetsManager
    }
}
