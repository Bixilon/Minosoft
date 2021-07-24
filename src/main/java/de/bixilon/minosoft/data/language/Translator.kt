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
import de.bixilon.minosoft.data.registries.registries.registry.Translatable
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent

interface Translator {

    fun canTranslate(key: ResourceLocation?): Boolean

    fun translate(key: ResourceLocation?, parent: TextComponent? = null, vararg data: Any?): ChatComponent


    fun translate(translatable: Any?): ChatComponent {
        return when (translatable) {
            is ChatComponent -> translatable
            is Translatable -> translate(translatable.translationKey)
            else -> ChatComponent.of(translatable)
        }
    }
}
