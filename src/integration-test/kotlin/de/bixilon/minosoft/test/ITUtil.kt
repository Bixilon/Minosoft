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

package de.bixilon.minosoft.test

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.VersionRegistry
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.SkipException

object ITUtil {
    private val profile = createResourcesProfile()

    fun createResourcesProfile(): ResourcesProfile {
        return ResourcesProfile()
    }

    fun loadPixlyzerData(name: String): VersionRegistry {
        val version = Versions[name]!!
        val registries = loadPixlyzerData(version)
        return VersionRegistry(version, registries)
    }

    fun loadPixlyzerData(version: Version): Registries {
        val registries = Registries(false)

        val data = PixLyzerUtil.loadPixlyzerData(version, profile)

        registries.load(version, data, CountUpAndDownLatch(0))

        return registries
    }

    @Deprecated("Its not implemented")
    fun todo() {
        throw SkipException("Not yet implemented!")
    }
}
