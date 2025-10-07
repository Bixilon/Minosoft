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

import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt

@JvmInline
value class HideProperty(
    val hideFlags: Int = 0,
) : Property {

    val enchantments: Boolean
        get() = hideFlags.isBit(ENCHANTMENT_BIT)

    val modifiers: Boolean
        get() = hideFlags.isBit(MODIFIERS_BIT)

    val unbreakable: Boolean
        get() = hideFlags.isBit(UNBREAKABLE_BIT)

    val canDestroy: Boolean
        get() = hideFlags.isBit(CAN_DESTROY_BIT)

    val canPlaceOn: Boolean
        get() = hideFlags.isBit(CAN_PLACE_BIT)

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    val otherInformation: Boolean
        get() = hideFlags.isBit(OTHER_INFORMATION_BIT)


    val leatherDyeColor: Boolean
        get() = hideFlags.isBit(LEATHER_DYE_COLOR_BIT)



    fun updateNbt(nbt: MutableJsonObject): Boolean {
        nbt.remove(HIDE_FLAGS_TAG)?.toInt()?.let { this.hideFlags = it }
        return !isDefault()
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
