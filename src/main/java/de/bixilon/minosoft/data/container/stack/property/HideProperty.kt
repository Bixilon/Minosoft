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

package de.bixilon.minosoft.data.container.stack.property

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.InventoryDelegate
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.util.BitByte.isBit

class HideProperty(
    private val stack: ItemStack,
    hideFlags: Int = 0,
) : Property {
    var _hideFlags = hideFlags
    var hideFlags by InventoryDelegate(stack, this::_hideFlags)

    var enchantments: Boolean
        get() = hideFlags.isBit(ENCHANTMENT_BIT)
        set(value) = setHideFlag(ENCHANTMENT_BIT, value)

    var modifiers: Boolean
        get() = hideFlags.isBit(MODIFIERS_BIT)
        set(value) = setHideFlag(MODIFIERS_BIT, value)

    var unbreakable: Boolean
        get() = hideFlags.isBit(UNBREAKABLE_BIT)
        set(value) = setHideFlag(UNBREAKABLE_BIT, value)

    var canDestroy: Boolean
        get() = hideFlags.isBit(CAN_DESTROY_BIT)
        set(value) = setHideFlag(CAN_DESTROY_BIT, value)

    var canPlaceOn: Boolean
        get() = hideFlags.isBit(CAN_PLACE_BIT)
        set(value) = setHideFlag(CAN_PLACE_BIT, value)

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    var otherInformation: Boolean
        get() = hideFlags.isBit(OTHER_INFORMATION_BIT)
        set(value) = setHideFlag(OTHER_INFORMATION_BIT, value)


    var leatherDyeColor: Boolean
        get() = hideFlags.isBit(LEATHER_DYE_COLOR_BIT)
        set(value) = setHideFlag(LEATHER_DYE_COLOR_BIT, value)


    private fun setHideFlag(bit: Int, setOrRemove: Boolean) {
        val mask = (1 shl bit)
        hideFlags = if (setOrRemove) {
            hideFlags or mask
        } else {
            hideFlags and mask.inv()
        }
    }

    override fun isDefault(): Boolean {
        return _hideFlags == 0
    }

    override fun updateNbt(nbt: MutableJsonObject): Boolean {
        nbt.remove(HIDE_FLAGS_TAG)?.toInt()?.let { this._hideFlags = it }
        return !isDefault()
    }

    override fun hashCode(): Int {
        return _hideFlags
    }

    override fun equals(other: Any?): Boolean {
        if (other !is HideProperty) {
            return false
        }
        return _hideFlags == other._hideFlags
    }

    fun copy(
        stack: ItemStack,
        hideFlags: Int = this.hideFlags,
    ): HideProperty {
        return HideProperty(stack, hideFlags)
    }


    companion object {
        private const val HIDE_FLAGS_TAG = "HideFlags"

        private const val ENCHANTMENT_BIT = 0
        private const val MODIFIERS_BIT = 1
        private const val UNBREAKABLE_BIT = 2
        private const val CAN_DESTROY_BIT = 3
        private const val CAN_PLACE_BIT = 4
        private const val OTHER_INFORMATION_BIT = 5
        private const val LEATHER_DYE_COLOR_BIT = 6
    }
}
