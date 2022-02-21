/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.inventory.stack.property

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.observeList
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.watchedList
import de.bixilon.minosoft.data.inventory.InventoryDelegate
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

class DisplayProperty(
    private val stack: ItemStack,
    lore: MutableList<ChatComponent> = mutableListOf(),
    customDisplayName: ChatComponent? = null,
    dyedColor: RGBColor? = null,
) : Property {
    val lore by watchedList(lore) // ToDo: Lock
    var _customDisplayName = customDisplayName
    var customDisplayName by InventoryDelegate(stack, this::_customDisplayName)
    var _dyeColor = dyedColor
    var dyedColor by InventoryDelegate(stack, this::_dyeColor)


    init {
        this::lore.observeList(this) { stack.holder?.container?.let { it.revision++ } }
    }

    override fun updateNbt(nbt: MutableJsonObject) {
        val display = nbt.remove(DISPLAY_TAG)?.nullCast<JsonObject>() ?: return
        display[DISPLAY_MAME_TAG]?.let { _customDisplayName = ChatComponent.of(it, translator = stack.holder?.connection?.language) }

        display[DISPLAY_LORE_TAG]?.listCast<String>()?.let {
            for (line in it) {
                this.lore += ChatComponent.of(line, translator = stack.holder?.connection?.language)
            }
        }

        display[DISPLAY_COLOR_TAG]?.toInt()?.asRGBColor()?.let { this._dyeColor = it }
    }

    fun copy(
        stack: ItemStack,
        lore: MutableList<ChatComponent> = this.lore.toMutableList(),
        customDisplayName: ChatComponent? = this.customDisplayName,
        dyedColor: RGBColor? = this.dyedColor,
    ): DisplayProperty {
        return DisplayProperty(stack, lore, customDisplayName, dyedColor)
    }

    companion object {
        private const val DISPLAY_TAG = "display"
        private const val DISPLAY_MAME_TAG = "Name"
        private const val DISPLAY_LORE_TAG = "Lore"
        private const val DISPLAY_COLOR_TAG = "color"
    }
}
