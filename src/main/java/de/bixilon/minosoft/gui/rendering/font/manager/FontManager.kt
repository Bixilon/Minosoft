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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.loader.DefaultFontIndices
import de.bixilon.minosoft.gui.rendering.font.loader.FontLoader
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.PostInitFontType
import de.bixilon.minosoft.gui.rendering.font.types.font.EmptyFont
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class FontManager(default: FontType? = null) {
    private val fonts: MutableMap<ResourceLocation, FontType> = mutableMapOf()
    var default = default ?: unsafeNull()

    init {
        if (default != null) {
            fonts[DefaultFontIndices.DEFAULT.fontName()] = default
        }
    }

    fun postInit(latch: AbstractLatch) {
        for (font in fonts.values) {
            if (font !is PostInitFontType) continue
            font.postInit(latch)
        }
    }


    operator fun get(name: ResourceLocation?) = fonts[name?.fontName()]

    fun load(index: ResourceLocation, context: RenderContext, latch: AbstractLatch?): FontType? {
        val name = index.fontName()
        fonts[name]?.let { return it }


        val font = FontLoader.load(context, this, index, latch)
        if (font == null) {
            Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Font $index seems to be empty!" }
            return null
        }
        val type = font.trim() ?: return null

        this.fonts[name] = type
        return type
    }

    companion object {

        fun create(context: RenderContext, latch: AbstractLatch): FontManager {
            val manager = FontManager()

            val default = manager.load(DefaultFontIndices.DEFAULT, context, latch) ?: EmptyFont
            manager::default.forceSet(default)

            for (index in DefaultFontIndices.ALL) {
                manager.load(index, context, latch)
            }

            return manager
        }

        private fun ResourceLocation.fontName(): ResourceLocation {
            return ResourceLocation(namespace, path.removePrefix("font/").removeSuffix(".json"))
        }
    }
}
