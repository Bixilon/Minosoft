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

package de.bixilon.minosoft.data.language

import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.data.language.manager.MultiLanguageManager
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object IntegratedLanguage {
    val LANGUAGE = MultiLanguageManager()


    fun load(name: String) {
        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loading language files (${name})" }
        val language = LanguageUtil.load(name, null, IntegratedAssets.DEFAULT, minosoft("language/"))
        LANGUAGE.translators[Namespaces.MINOSOFT] = language
    }
}
