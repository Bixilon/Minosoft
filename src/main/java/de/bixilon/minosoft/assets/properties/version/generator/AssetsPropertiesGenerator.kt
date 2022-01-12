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

package de.bixilon.minosoft.assets.properties.version.generator

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.InvalidAssetException
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.VersionTypes

object AssetsPropertiesGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            throw IllegalArgumentException("Usage: application <version id> <client jar hash>")
        }
        // create profile to not make crashes (or load an actual profile)
        val profile = ResourcesProfileManager.createProfile()
        profile.verify = false
        val (versionId, clientJarHash) = args

        val assetsManager = JarAssetsManager("abcdef", clientJarHash, profile, Version(versionId, -1, -1, VersionTypes.APRIL_FOOL, mapOf(), mapOf()))
        try {
            assetsManager.load(CountUpAndDownLatch(1))
        } catch (exception: InvalidAssetException) {
            // this exception is thrown, because our initial has is "dummy"
            print(exception.hash)
        }
    }
}