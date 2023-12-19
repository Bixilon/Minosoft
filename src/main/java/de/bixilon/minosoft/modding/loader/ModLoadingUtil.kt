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

package de.bixilon.minosoft.modding.loader

import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.modding.loader.error.MissingDependencyError
import de.bixilon.minosoft.modding.loader.error.NoManifestError
import de.bixilon.minosoft.modding.loader.error.NoModMainError
import de.bixilon.minosoft.modding.loader.error.NoObjectMainError
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.modding.loader.mod.logger.ModLogger


object ModLoadingUtil {


    fun MinosoftMod.construct() {
        val manifest = manifest ?: throw NoManifestError(source)
        val mainClass = Class.forName(manifest.main, true, classLoader)
        val main = mainClass.kotlin.objectInstance ?: throw NoObjectMainError(mainClass)
        if (main !is ModMain) {
            throw NoModMainError(mainClass)
        }
        ASSETS_MANAGER_FIELD[main] = assetsManager!!
        LOGGER_FIELD[main] = ModLogger(manifest.name)
        this.main = main
        main.init()
    }

    fun MinosoftMod.postInit() {
        main!!.postInit()
    }


    fun MinosoftMod.validate() {
        val manifest = manifest!!
        val missingDependencies = manifest.packages?.getMissingDependencies(ModLoader.mods)
        if (missingDependencies != null) {
            throw MissingDependencyError(this, missingDependencies)
        }
    }


    private val MOD_MAIN = ModMain::class.java
    private val ASSETS_MANAGER_FIELD = MOD_MAIN.getFieldOrNull("assets")!!
    private val LOGGER_FIELD = MOD_MAIN.getFieldOrNull("logger")!!
}
