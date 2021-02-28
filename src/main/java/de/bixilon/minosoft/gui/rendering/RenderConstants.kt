/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.text.RGBColor

object RenderConstants {
    val DEFAULT_SKY_COLOR = RGBColor("#ecff89")
    val WHITE_COLOR = RGBColor("#ffffff")
    val BLACK_COLOR = RGBColor("#000000")

    val GRASS_FAILOVER_COLOR = RGBColor("#48B518")

    val GRASS_OUT_OF_BOUNDS_COLOR = RGBColor(-65281)

    val LILY_PAD_INVENTORY_COLOR = RGBColor("#71C35C")
    val LILY_PAD_BLOCK_COLOR = RGBColor("#208030")

    const val COLORMAP_SIZE = 255

    const val DEBUG_MESSAGES_PREFIX = "§f[§e§lDEBUG§f] §9"
}
