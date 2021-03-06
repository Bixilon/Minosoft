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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import java.util.*

class MultiLanguageManager(
    val translators: MutableMap<String, Translator> = mutableMapOf(),
) : Translator {

    override fun canTranslate(key: ResourceLocation?): Boolean {
        key ?: return false
        return translators[key.namespace]?.canTranslate(key) == true
    }

    override fun translate(key: ResourceLocation?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        key ?: return ChatComponent.of("null: ${data.contentToString()}")

        return translators[key.namespace]?.translate(key, parent, *data) ?: ChatComponent.of("$key: ${data.contentToString()}")
    }

    fun loadLanguage(language: Locale) {

    }
}
