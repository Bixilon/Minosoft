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

package de.bixilon.minosoft.data.inventory

object ItemNBTValues {
    const val REPAIR_COST_TAG = "RepairCost"
    const val DISPLAY_TAG = "display"
    const val DISPLAY_MAME_TAG = "Name"
    const val DISPLAY_LORE_TAG = "Lore"
    const val DISPLAY_COLOR_TAG = "color"
    const val UNBREAKABLE_TAG = "unbreakable"
    const val HIDE_FLAGS_TAG = "HideFlags"

    const val ENCHANTMENT_FLATTENING_TAG = "Enchantments"
    const val ENCHANTMENT_PRE_FLATTENING_TAG = "ench"
    val ENCHANTMENTS_TAG = arrayOf(ENCHANTMENT_FLATTENING_TAG, ENCHANTMENT_PRE_FLATTENING_TAG)
    const val ENCHANTMENT_ID_TAG = "id"
    const val ENCHANTMENT_LEVEL_TAG = "lvl"
}
