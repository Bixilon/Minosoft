/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound

class LanguageManager(
    private val languages: MutableList<Language> = synchronizedListOf(),
) : Translator {

    override fun canTranslate(key: ResourceLocation?): Boolean {
        for (language in languages) {
            if (language.canTranslate(key)) {
                return true
            }
        }
        return false
    }

    override fun translate(key: ResourceLocation?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        for (language in languages) {
            if (!language.canTranslate(key)) {
                continue
            }
            return language.translate(key, parent, *data)
        }
        return ChatComponent.of("$key: ${data.contentToString()}")
    }

    companion object {


        fun load(language: String, version: Version?, path: ResourceLocation = ResourceLocation("lang/")): LanguageManager {
            val assetsManager = version?.assetsManager ?: Minosoft.MINOSOFT_ASSETS_MANAGER

            fun loadMinecraftLanguage(language: String): Language {
                val data: MutableMap<ResourceLocation, String> = mutableMapOf()

                if (version?.versionId ?: Int.MIN_VALUE >= ProtocolVersions.V_18W02A) {
                    for ((key, value) in assetsManager.readJsonAsset(ResourceLocation(path.namespace, path.path + "${language.lowercase()}.json")).asCompound()) {
                        data[ResourceLocation(key)] = value.toString()
                    }
                } else {
                    val lines = assetsManager.readStringAsset(ResourceLocation(path.namespace, path.path + "${language.lowercase()}.lang")).lines()

                    for (line in lines) {
                        if (line.isBlank()) {
                            continue
                        }
                        val split = line.split('=', limit = 2)
                        data[ResourceLocation(split[0])] = split[1]
                    }
                }

                return Language(language, data)
            }

            val languages: MutableList<Language> = mutableListOf()

            if (language != "en_US") {
                languages += loadMinecraftLanguage(language)
            }
            languages += loadMinecraftLanguage("en_US")

            return LanguageManager(languages)

        }
    }
}
