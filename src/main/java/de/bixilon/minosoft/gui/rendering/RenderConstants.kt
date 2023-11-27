/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture

object RenderConstants {
    const val COLORMAP_SIZE = 255


    val TEXT_BACKGROUND_COLOR = RGBColor(0, 0, 0, 80)


    const val FRUSTUM_CULLING_ENABLED = true
    const val OCCLUSION_CULLING_ENABLED = true
    const val SHOW_FPS_IN_WINDOW_TITLE = true

    const val MAXIMUM_QUEUE_TIME_PER_FRAME = 20L


    val DEBUG_TEXTURE_RESOURCE_LOCATION = minosoft("debug").texture()


    const val DEFAULT_LINE_WIDTH = 1.0f / 128.0f


    const val UV_ADD = 0.00001f

    const val DISABLE_GUI_CACHE = false

    const val DIRTY_BUFFER_UNBIND = true
}
