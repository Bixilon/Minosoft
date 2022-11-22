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

package de.bixilon.minosoft.test

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions

object ITUtil {
    private val profile = createResourcesProfile()

    fun createResourcesProfile(): ResourcesProfile {
        ResourcesProfileManager.currentLoadingPath = "dummy"
        val profile = ResourcesProfile()
        ResourcesProfileManager.currentLoadingPath = null
        return profile
    }

    fun loadPixlyzerData(name: String): Version {
        val version = Versions[name]!!

        loadPixlyzerData(version)
        return version
    }

    fun loadPixlyzerData(version: Version) {
        version.load(profile, CountUpAndDownLatch(0))
    }
}
