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

package de.bixilon.minosoft.data.language

import de.bixilon.kutil.exception.ExceptionUtil
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.FileUtil.readAsString
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.language.lang.Language
import de.bixilon.minosoft.data.language.lang.LanguageData
import de.bixilon.minosoft.data.language.lang.LanguageList
import de.bixilon.minosoft.data.language.manager.LanguageManager
import de.bixilon.minosoft.data.language.translate.Translated
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.io.FileNotFoundException

object LanguageUtil {
    const val FALLBACK_LANGUAGE = "en_us"


    fun String?.i18n(): Translated {
        val resourceLocation = this.toResourceLocation()
        if (resourceLocation.namespace == ProtocolDefinition.DEFAULT_NAMESPACE) {
            return Translated(minosoft(resourceLocation.path))
        }
        return Translated(resourceLocation)
    }

    fun loadJsonLanguage(json: JsonObject): LanguageData {
        val data: LanguageData = mutableMapOf()

        for ((key, value) in json) {
            val path = ResourceLocation(key).path
            data[path] = value.toString().correctValue()
        }

        return data
    }

    fun loadLanguage(lines: Sequence<String>): LanguageData {
        val data: LanguageData = mutableMapOf()

        for (line in lines) {
            if (line.isBlank() || line.startsWith("#")) {
                continue
            }
            val (key, value) = line.split('=', limit = 2)
            val path = ResourceLocation(key).path
            data[path] = value.correctValue()
        }
        return data
    }

    private fun String.correctValue(): String {
        return this.replace("\\n", "\n")
    }

    fun getFallbackTranslation(key: ResourceLocation?, parent: TextComponent?, restrictedMode: Boolean = false, vararg data: Any?): ChatComponent {
        if (data.isEmpty()) {
            return ChatComponent.of(key.toString(), null, parent, restrictedMode)
        }
        return ChatComponent.of(key.toString() + "->" + data.contentToString(), null, parent, restrictedMode)
    }

    fun loadLanguage(language: String, assetsManager: AssetsManager, json: Boolean, path: ResourceLocation): Translator {
        val assets = assetsManager.getAll(ResourceLocation(path.namespace, path.path + language + if (json) ".json" else ".lang"))
        val languages: MutableList<Language> = mutableListOf()

        for (asset in assets) {
            val data = if (json) loadJsonLanguage(asset.readJsonObject()) else loadLanguage(asset.readAsString().lineSequence())
            languages += Language(language, data)
        }


        if (languages.size == 1) {
            return languages.first()
        }
        return LanguageList(languages)
    }


    fun load(language: String, version: Version?, assetsManager: AssetsManager, path: ResourceLocation = ResourceLocation("lang/")): Translator {
        val name = language.lowercase()
        val json = version != null && version.jsonLanguage

        val languages: MutableList<Translator> = mutableListOf()


        if (name != FALLBACK_LANGUAGE) {
            ExceptionUtil.tryCatch(FileNotFoundException::class.java, executor = { languages += loadLanguage(name, assetsManager, json, path) })
        }
        languages += loadLanguage(FALLBACK_LANGUAGE, assetsManager, json, path)

        if (languages.size == 1) {
            return languages.first()
        }

        return LanguageManager(languages)
    }


    fun ResourceLocation.translation(name: String): ResourceLocation {
        return ResourceLocation(this.namespace, "item.$namespace.$path")
    }
}
