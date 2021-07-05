/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.locale.minecraft

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import java.util.*

class MinecraftLocaleManager(private val version: Version) : Translator {
    lateinit var language: MinecraftLanguage
        private set

    override fun translate(key: String?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        return language.translate(key, parent, *data)
    }

    private fun loadLanguage(version: Version, language: String): MinecraftLanguage {
        return if (version.versionId >= ProtocolVersions.V_18W02A) {
            MinecraftLanguage(language, this.version.assetsManager.readJsonAsset(ResourceLocation(String.format("lang/%s.json", language.lowercase(Locale.getDefault())))).compoundCast()!!)
        } else {
            MinecraftLanguage(language, this.version.assetsManager.readStringAsset(ResourceLocation(String.format("lang/%s.lang", language.lowercase(Locale.getDefault())))))
        }
    }

    fun load(version: Version, language: String) {
        val startTime = System.currentTimeMillis()
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.INFO) { "Loading minecraft $language language files for $version..." }
        Log.verbose(String.format("Loading minecraft language file (%s) for %s", language, this.version))
        try {
            this.language = loadLanguage(version, language)
        } catch (exception: Exception) {
            Log.log(LogMessageType.VERSION_LOADING, LogLevels.WARN) { "Could not load minecraft language files for $version (language=$language)!" }
            return
        }
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.INFO) { "Loaded minecraft language files for $version successfully in ${System.currentTimeMillis() - startTime}ms" }
    }

    fun canTranslate(key: String?): Boolean {
        return language.canTranslate(key)
    }
}
