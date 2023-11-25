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

package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

// TODO: support size override
class UnihexFontType(
    val chars: Int2ObjectOpenHashMap<CodePointRenderer>,
) : FontType {

    override fun get(codePoint: Int): CodePointRenderer? {
        return chars[codePoint]
    }


    companion object : FontTypeFactory<UnihexFontType> {
        override val identifier = minecraft("unihex")

        override fun build(context: RenderContext, manager: FontManager, data: JsonObject): UnihexFontType? {
            val hexFile = data["hex_file"]?.toResourceLocation() ?: throw IllegalArgumentException("hex_file missing!")
            val sizes = data["size_override"]?.listCast<JsonObject>()?.let { SizeOverride.deserialize(it) } ?: emptyList()

            return load(context, hexFile, sizes)
        }

        fun load(context: RenderContext, hexFile: ResourceLocation, sizes: List<SizeOverride>): UnihexFontType? {
            val stream = ZipInputStream(context.connection.assetsManager[hexFile])
            val buffered = BufferedInputStream(stream)

            val chars = Int2ObjectOpenHashMap<ByteArray>()
            var totalWidth = 0
            while (true) {
                val entry = stream.nextEntry ?: break
                if (!entry.name.endsWith(".hex")) continue

                totalWidth += buffered.readUnihex(chars)
            }
            if (chars.isEmpty()) return null

            return load(context, totalWidth, chars, sizes)
        }

        private fun load(context: RenderContext, totalWidth: Int, chars: Int2ObjectOpenHashMap<ByteArray>, sizes: List<SizeOverride>): UnihexFontType {
            val rasterizer = UnifontRasterizer(context.textures.font, totalWidth)

            val rasterized = Int2ObjectOpenHashMap<CodePointRenderer>()

            for (entry in chars.int2ObjectEntrySet().fastIterator()) {
                rasterized[entry.intKey] = rasterizer.add(entry.value)
            }

            rasterized.trim()

            return UnihexFontType(rasterized)
        }

        fun Int.fromHex(): Int {
            return this - when (this) {
                in '0'.code..'9'.code -> '0'.code
                in 'a'.code..'f'.code -> 'a'.code - 0x0A
                in 'A'.code..'F'.code -> 'A'.code - 0x0A
                else -> throw IllegalArgumentException("Invalid hex char: ${toChar()}!")
            }
        }

        private fun InputStream.readHexInt(): Int {
            var value = 0
            while (true) {
                val byte = this.read()
                if (byte < 0 || byte == '\n'.code) return -1
                if (byte == ':'.code) break // separator
                val hex = byte.fromHex()

                value = (value shl 4) or hex
            }

            return value
        }

        private fun InputStream.readUnihexData(buffer: ByteArray): ByteArray {
            var index = 0

            while (true) {
                val byte = this.read()
                if (byte < 0) break
                if (byte == '\n'.code) break // separator
                val hex = byte.fromHex()

                if (index % 2 == 0) {
                    // most significant bits
                    buffer[index / 2] = ((buffer[index / 2].toInt() and 0x0F) or (hex shl 4)).toByte()
                } else {
                    buffer[index / 2] = ((buffer[index / 2].toInt() and 0xF0) or hex).toByte()
                }
                index++
            }

            val array = ByteArray(index / 2)
            System.arraycopy(buffer, 0, array, 0, array.size)
            return array
        }

        private fun InputStream.readUnihex(chars: Int2ObjectOpenHashMap<ByteArray>): Int {
            val buffer = ByteArray(64)
            var totalWidth = 0
            while (this.available() > 0) {
                val codePoint = readHexInt()
                if (codePoint == -1) continue
                val data = this.readUnihexData(buffer)
                chars[codePoint] = data
                totalWidth += data.size / (UnifontRasterizer.HEIGHT / Byte.SIZE_BITS)
            }

            return totalWidth
        }
    }
}
