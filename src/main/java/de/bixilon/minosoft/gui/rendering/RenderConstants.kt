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


    val EXPERIENCE_BAR_LEVEL_COLOR = RGBColor("#80ff20")
    val HP_TEXT_COLOR = RGBColor("#ff1313")

    const val COLORMAP_SIZE = 255

    const val DEBUG_MESSAGES_PREFIX = "§f[§e§lDEBUG§f] §9"


    const val HUD_Z_COORDINATE = -0.9996f
    const val HUD_Z_COORDINATE_Z_FACTOR = -0.000001f

    val TEXT_BACKGROUND_COLOR = RGBColor(0, 0, 0, 80)

    const val TEXT_LINE_PADDING = 0

    const val CHUNK_SECTIONS_PER_MESH = 1

    const val FRUSTUM_CULLING_ENABLED = true

    const val MAXIMUM_CALLS_PER_FRAME = 10
}
