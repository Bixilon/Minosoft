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

package de.bixilon.minosoft.assets.properties.version.generator

import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.minosoft.assets.InvalidAssetException
import de.bixilon.minosoft.assets.minecraft.JarAssetsManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.logging.Log
import org.objenesis.ObjenesisStd
import kotlin.system.exitProcess

object AssetsPropertiesGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        val stream = System.out
        System.setOut(System.err)
        Log::class.java.forceInit()
        Log.ASYNC_LOGGING = false
        if (args.size != 1) {
            throw IllegalArgumentException("Usage: application <client jar hash>")
        }
        // create profile to not make crashes (or load an actual profile)
        val profile = ResourcesProfileManager.createProfile()
        profile.verify = false
        val (clientJarHash) = args

        val version = ObjenesisStd().newInstance(Version::class.java)
        val assetsManager = JarAssetsManager("0000000000000000000000000000000000000000", clientJarHash, profile, version)
        try {
            assetsManager.load()
        } catch (exception: InvalidAssetException) {
            // this exception is thrown, because our initial hash is "dummy"
            stream.print(exception.hash + ":" + exception.tarBytes)
            exitProcess(0)
        }
        exitProcess(1)
    }
}
