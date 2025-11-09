/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import org.objenesis.ObjenesisStd
import org.testng.SkipException

object ITUtil {
    private val profile = ResourcesProfile()
    private val registries: MutableMap<Version, Registries> = mutableMapOf()
    private val objenesis = ObjenesisStd()


    private fun loadPixlyzerData(version: Version): Registries {
        val registries = Registries(false, version)

        val data = PixLyzerUtil.load(profile, version)

        registries.load(data, SimpleLatch(0))

        return registries
    }

    private fun loadPreFlatteningData(version: Version): Registries {
        return PreFlattening.loadRegistry(profile, version, SimpleLatch(0))
    }

    fun loadRegistries(version: Version) = registries.getOrPut(version) {
        if (version.flattened) loadPixlyzerData(version) else loadPreFlatteningData(version)
    }

    @Deprecated("Its not implemented")
    fun todo() {
        throw SkipException("Not yet implemented!")
    }

    fun <T> Class<T>.allocate(): T {
        return objenesis.newInstance(this)
    }
}
