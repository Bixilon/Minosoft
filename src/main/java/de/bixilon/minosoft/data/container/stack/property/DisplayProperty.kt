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

package de.bixilon.minosoft.data.container.stack.property

import com.google.common.base.Objects
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.observer.list.ListObserver.Companion.observeList
import de.bixilon.kutil.observer.list.ListObserver.Companion.observedList
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.InventoryDelegate
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

class DisplayProperty(
    private val stack: ItemStack,
    lore: MutableList<ChatComponent> = mutableListOf(),
    customDisplayName: ChatComponent? = null,
    dyedColor: RGBColor? = null,
) : Property {
    val lore by observedList(lore) // ToDo: Lock
    var _customDisplayName = customDisplayName
    var customDisplayName by InventoryDelegate(stack, this::_customDisplayName)

    @Deprecated("Should belong in DyeableItem")
    var _dyeColor = dyedColor
    var dyeColor by InventoryDelegate(stack, this::_dyeColor)


    init {
        this::lore.observeList(this) { stack.holder?.container?.let { it.revision++ } }
    }

    override fun isDefault(): Boolean {
        return _customDisplayName == null && lore.isEmpty() && _dyeColor == null
    }

    override fun updateNbt(nbt: MutableJsonObject): Boolean {
        val display = nbt.remove(DISPLAY_TAG)?.nullCast<JsonObject>() ?: return false
        display[DISPLAY_MAME_TAG]?.let { _customDisplayName = ChatComponent.of(it, translator = stack.holder?.connection?.language) }

        display[DISPLAY_LORE_TAG]?.listCast<String>()?.let {
            for (line in it) {
                this.lore += ChatComponent.of(line, translator = stack.holder?.connection?.language)
            }
        }

        display[DISPLAY_COLOR_TAG]?.toInt()?.asRGBColor()?.let { this._dyeColor = it }

        return !isDefault()
    }

    override fun hashCode(): Int {
        return Objects.hashCode(lore, _customDisplayName, _dyeColor)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DisplayProperty) {
            return false
        }
        if (other.hashCode() != hashCode()) {
            return false
        }
        return lore == other.lore && _customDisplayName == other._customDisplayName && _dyeColor == other._dyeColor
    }

    fun copy(
        stack: ItemStack,
        lore: MutableList<ChatComponent> = this.lore.toMutableList(),
        customDisplayName: ChatComponent? = this._customDisplayName,
        dyedColor: RGBColor? = this._dyeColor,
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
