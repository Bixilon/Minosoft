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

import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.assets.properties.version.PreFlattening
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.VersionRegistry
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.SkipException

object ITUtil {
    private val profile = createResourcesProfile()
    private val pixlyzer: MutableMap<Version, Registries> = mutableMapOf()


    fun createResourcesProfile(): ResourcesProfile {
        return ResourcesProfile()
    }

    fun loadPixlyzerData(name: String): VersionRegistry {
        val version = Versions[name]!!
        val registries = loadPixlyzerData(version)
        return VersionRegistry(version, registries)
    }

    fun loadPixlyzerData(version: Version): Registries {
        pixlyzer[version]?.let { return it }
        val registries = Registries(false)

        val data = PixLyzerUtil.loadPixlyzerData(profile, version)

        registries.load(version, data, SimpleLatch(0))
        pixlyzer[version] = registries

        return registries
    }

    fun loadPreFlatteningData(version: Version): Registries {
        return PreFlattening.loadRegistry(profile, version, SimpleLatch(0))
    }

    fun loadRegistries(version: Version): Registries {
        if (version.flattened) return loadPixlyzerData(version)
        return loadPreFlatteningData(version)
    }

    @Deprecated("Its not implemented")
    fun todo() {
        throw SkipException("Not yet implemented!")
    }

    fun <T> Class<T>.allocate(): T {
        return IT.OBJENESIS.newInstance(this)
    }
}
