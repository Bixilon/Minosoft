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
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

data class DisplayProperty(
    val lore: List<ChatComponent> = emptyList(),
    val customDisplayName: ChatComponent? = null,
    val dyeColor: RGBColor? = null,
) : Property {


    companion object {
        private const val DISPLAY_TAG = "display"
        private const val DISPLAY_MAME_TAG = "Name"
        private const val DISPLAY_LORE_TAG = "Lore"
        private const val DISPLAY_COLOR_TAG = "color"

        fun ItemStack.updateDisplayNbt(nbt: MutableJsonObject): Boolean {
            val display = nbt.remove(DISPLAY_TAG)?.nullCast<JsonObject>() ?: return false
            display[DISPLAY_MAME_TAG]?.let { this.display.customDisplayName = ChatComponent.of(it, translator = this.holder?.session?.language) }

            display[DISPLAY_LORE_TAG]?.listCast<String>()?.let {
                for (line in it) {
                    this.display.lore += ChatComponent.of(line, translator = this.holder?.session?.language)
                }
            }

            display[DISPLAY_COLOR_TAG]?.toInt()?.rgb()?.let { this.display._dyeColor = it }
            if (_display == null) return false

            return !this.display.isDefault()
        }
    }
}
