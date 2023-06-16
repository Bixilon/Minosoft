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

package de.bixilon.minosoft.gui.rendering.font.manager

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.loader.DefaultFontIndices
import de.bixilon.minosoft.gui.rendering.font.loader.FontLoader
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.PostInitFontType
import de.bixilon.minosoft.gui.rendering.font.types.font.EmptyFont
import de.bixilon.minosoft.gui.rendering.font.types.font.Font
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class FontManager(
    val default: FontType,
) {

    fun postInit(latch: AbstractLatch) {
        if (default is PostInitFontType) {
            default.postInit(latch)
        }
    }


    operator fun get(font: ResourceLocation?): Font? = null

    companion object {
        fun create(context: RenderContext, latch: AbstractLatch): FontManager {
            val font = FontLoader.load(context, DefaultFontIndices.DEFAULT, latch)

            // TODO: load multiple fonts

            if (font == null) {
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Font ${DefaultFontIndices.DEFAULT} seems to be empty!" }
            }

            return FontManager(font?.trim() ?: EmptyFont)
        }
    }
}
