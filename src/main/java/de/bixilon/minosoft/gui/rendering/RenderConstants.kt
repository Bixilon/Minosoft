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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor

object RenderConstants {
    val DEFAULT_SKY_COLOR = "#ecff89".asColor()
    val BLACK_COLOR = "#000000".asColor()

    val GRASS_FAILOVER_COLOR = "#48B518".asColor()


    val EXPERIENCE_BAR_LEVEL_COLOR = "#80ff20".asColor()

    const val COLORMAP_SIZE = 255

    const val DEBUG_MESSAGES_PREFIX = "§f[§e§lDEBUG§f] "

    val TEXT_BACKGROUND_COLOR = RGBColor(0, 0, 0, 80)

    const val TEXT_LINE_PADDING = 2
    val WORD_SEPARATORS = arrayOf(' ', '.', ',', '!', '-', '?')

    const val FRUSTUM_CULLING_ENABLED = true
    const val SHOW_FPS_IN_WINDOW_TITLE = true

    const val MAXIMUM_QUEUE_TIME_PER_FRAME = 20L

    const val DISABLE_LIGHTING = false

    const val RENDER_BLOCKS = true
    const val RENDER_FLUIDS = true
    const val RENDER_HUD = true


    val DEBUG_TEXTURE_RESOURCE_LOCATION = ResourceLocation("minosoft:textures/debug.png")
    const val DEBUG_TEXTURE_ID = 0 // always add the debug texture to the texture array first to ensure the id is 0


    const val CAMPFIRE_ITEMS = 4

    const val DOUBLE_PRESS_KEY_PRESS_MAX_DELAY = 300
    const val DOUBLE_PRESS_DELAY_BETWEEN_PRESSED = 500

    const val MAXIMUM_PARTICLE_AMOUNT = 500000

    const val DEFAULT_LINE_WIDTH = 1.0f / 128.0f


    const val UV_ADD = 0.001f

    const val DISABLE_GUI_CACHE = false
}
