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

package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class BitmapFontType(
    val chars: Int2ObjectOpenHashMap<BitmapCodeRenderer>,
) : FontType {

    init {
        chars.trim()
    }

    override fun get(codePoint: Int): BitmapCodeRenderer {
        return chars[codePoint]
    }

    companion object : FontTypeFactory<BitmapFontType> {
        override val identifier = minecraft("bitmap")

        override fun build(context: RenderContext, data: JsonObject): BitmapFontType? {
            val file = data["file"]?.toString()?.let { it.toResourceLocation().texture() } ?: throw IllegalArgumentException("Missing file!")
            val height = data["height"]?.toInt() ?: 8
            val ascent = data["ascent"]?.toInt() ?: 8
            val chars = data["chars"]?.listCast<String>() ?: throw IllegalArgumentException("Missing chars!")
            return load(file, height, ascent, chars, context)
        }

        private fun load(file: ResourceLocation, height: Int, ascent: Int, chars: List<String>, context: RenderContext): BitmapFontType? {
            val texture = context.textureManager.staticTextures.createTexture(file)
            texture.load(context.connection.assetsManager) // force load it, we need to calculate the width of every char

            // TODO
            return null
        }
    }
}
