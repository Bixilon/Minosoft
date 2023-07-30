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

package de.bixilon.minosoft.modding.loader.mod

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.modding.loader.LoaderUtil.unloadAll
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.mod.manifest.ModManifest
import de.bixilon.minosoft.modding.loader.mod.source.ModSource
import org.xeustechnologies.jcl.JarClassLoader

class MinosoftMod(
    val source: ModSource,
    val phase: LoadingPhases,
    val latch: AbstractLatch,
) : Comparable<MinosoftMod> {
    val classLoader = JarClassLoader()
    var manifest: ModManifest? = null
    var assetsManager: AssetsManager? = null
    var main: ModMain? = null


    fun unload() {
        try {
            main = null
            classLoader.unloadAll()
            assetsManager?.unload()
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    override fun compareTo(other: MinosoftMod): Int {
        val manifest = manifest!!
        val otherManifest = other.manifest!!

        val depends: MutableSet<String> = mutableSetOf()
        manifest.packages?.depends?.let { depends += it }
        manifest.load?.after?.let { depends += it }

        if (otherManifest.name in depends) {
            return 1 // load before with higher priority than rest
        }

        manifest.load?.before?.let {
            if (otherManifest.name in it) {
                return -1 // after
            }
        }

        return 0
    }
}
