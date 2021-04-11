/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.locale.minecraft

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log

class MinecraftLocaleManager(private val version: Version) : Translator {
    lateinit var language: MinecraftLanguage
        private set

    override fun translate(key: String?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        return language.translate(key, parent, *data)
    }

    private fun loadLanguage(version: Version, language: String): MinecraftLanguage {
        return if (version.versionId >= ProtocolVersions.V_18W02A) {
            MinecraftLanguage(language, this.version.assetsManager.readJsonAsset(ResourceLocation(String.format("lang/%s.json", language.toLowerCase()))).asJsonObject)
        } else {
            MinecraftLanguage(language, this.version.assetsManager.readStringAsset(ResourceLocation(String.format("lang/%s.lang", language.toLowerCase()))))
        }
    }

    fun load(version: Version, language: String) {
        val startTime = System.currentTimeMillis()
        Log.verbose(String.format("Loading minecraft language file (%s) for %s", language, this.version))
        try {
            this.language = loadLanguage(version, language)
        } catch (exception: Exception) {
            Log.warn("Could not load minecraft language file: %s for %s", language, this.version)
            throw exception
        }
        Log.verbose("Loaded minecraft language files for %s successfully in %dms", this.version, System.currentTimeMillis() - startTime)
    }

    fun canTranslate(key: String?): Boolean {
        return language.canTranslate(key)
    }
}
