/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.stack.properties

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

data class DisplayProperty(
    val displayName: ChatComponent? = null,
    val lore: List<ChatComponent> = emptyList(),
    val dyeColor: RGBColor? = null,
) : Property {

    override fun writeNbt(item: Item, version: Version, registries: Registries, nbt: MutableJsonObject) {
        if (this == DEFAULT) return

        val display: MutableJsonObject = mutableMapOf()
        this.displayName?.let { display[DISPLAY_MAME_TAG] = it.toNbt() }
        this.lore.takeIf { it.isNotEmpty() }?.let { display[DISPLAY_LORE_TAG] = it.map(ChatComponent::toNbt) }

        this.dyeColor?.let { display[DISPLAY_COLOR_TAG] = "#${it.rgb.toHex(6)}" } // TODO: only if item is dyeable?

        nbt[DISPLAY_TAG] = display
    }


    companion object {
        val DEFAULT = DisplayProperty()
        private const val DISPLAY_TAG = "display"
        private const val DISPLAY_MAME_TAG = "Name"
        private const val DISPLAY_LORE_TAG = "Lore"
        private const val DISPLAY_COLOR_TAG = "color"

        fun of(translator: Translator?, nbt: MutableJsonObject): DisplayProperty {
            val display = nbt.remove(DISPLAY_TAG)?.nullCast<JsonObject>()?.takeIf { it.isNotEmpty() } ?: return DEFAULT

            val displayName = display[DISPLAY_MAME_TAG]?.let { ChatComponent.of(it, translator = translator) }

            val lore = display[DISPLAY_LORE_TAG]?.listCast<String>()?.takeIf { it.isNotEmpty() }?.let {
                val lore = ArrayList<ChatComponent>(it.size)
                for (line in it) {
                    lore += ChatComponent.of(line, translator = translator)
                }

                return@let lore
            }

            val dyeColor = display[DISPLAY_COLOR_TAG]?.toInt()?.rgb()

            if (displayName == null && (lore == null || lore.isEmpty()) && dyeColor == null) return DEFAULT

            return DisplayProperty(displayName, lore ?: emptyList(), dyeColor)
        }
    }
}
