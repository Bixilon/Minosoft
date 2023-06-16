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

package de.bixilon.minosoft.gui.rendering.font.types.empty

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class EmptyFontType(
    val chars: Int2ObjectOpenHashMap<EmptyCodeRenderer>,
) : FontType {

    init {
        chars.trim()
    }

    override fun get(codePoint: Int): CodePointRenderer? {
        return chars[codePoint]
    }

    companion object : FontTypeFactory<EmptyFontType>, AliasedIdentified {
        override val identifier = minosoft("empty")
        override val identifiers = setOf(minecraft("space"))

        override fun build(context: RenderContext, data: JsonObject): EmptyFontType? {
            val chars = load(data)
            if (chars == null) {
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Empty font provider: $data!" }
                return null
            }
            return EmptyFontType(chars)
        }


        fun load(data: JsonObject): Int2ObjectOpenHashMap<EmptyCodeRenderer>? {
            val advances = data["advances"]?.toJsonObject() ?: return null

            val spaces = Int2ObjectOpenHashMap<EmptyCodeRenderer>(advances.size, 0.01f)
            for ((char, spacing) in advances) {
                val codePoint = char.codePointAt(0)

                spaces[codePoint] = EmptyCodeRenderer(spacing.toInt())
            }

            if (spaces.isEmpty()) return null
            spaces.trim()

            return spaces
        }
    }
}
