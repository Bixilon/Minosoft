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

package de.bixilon.minosoft.data.assets

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.fromJson
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels

@Deprecated("Will be refactored soon!")
interface AssetsManager {

    val namespaces: Set<String>

    fun getAssetURL(resourceLocation: ResourceLocation): URL

    fun getAssetSize(resourceLocation: ResourceLocation): Long

    fun readAssetAsStream(resourceLocation: ResourceLocation): InputStream

    fun readAssetAsReader(resourceLocation: ResourceLocation): BufferedReader {
        return BufferedReader(InputStreamReader(readAssetAsStream(resourceLocation)))
    }

    @Deprecated(message = "Will be removed...")
    fun readLegacyJsonAsset(resourceLocation: ResourceLocation): JsonObject {
        val reader = readAssetAsReader(resourceLocation)
        val json = JsonParser.parseReader(reader).asJsonObject
        reader.close()
        return json
    }

    fun readJsonAsset(resourceLocation: ResourceLocation): Map<String, Any> {
        return readStringAsset(resourceLocation).fromJson().asCompound()
    }

    fun readStringAsset(resourceLocation: ResourceLocation): String {
        return Util.readReader(readAssetAsReader(resourceLocation), true)
    }

    fun readRGBArrayAsset(resourceLocation: ResourceLocation): IntArray {
        val decoder = PNGDecoder(readAssetAsStream(resourceLocation))

        val buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGB.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGB.numComponents, PNGDecoder.Format.RGB)
        buffer.flip()
        val colors = IntArray(decoder.width * decoder.height)

        for (i in colors.indices) {
            colors[i] = ((buffer.get().toInt() and 0xFF) shl 16) or ((buffer.get().toInt() and 0xFF) shl 8) or (buffer.get().toInt() and 0xFF)
        }

        return colors
    }

    fun readByteAsset(resourceLocation: ResourceLocation): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(getAssetSize(resourceLocation).toInt())
        val inputStream = readAssetAsStream(resourceLocation)
        val byteChannel = Channels.newChannel(inputStream)
        while (true) {
            val bytes: Int = byteChannel.read(buffer)
            if (bytes <= 0) {
                break
            }
        }
        byteChannel.close()
        inputStream.close()

        buffer.flip()
        return buffer
    }
}
