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

package de.bixilon.minosoft.assets.util

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.kutil.buffer.BufferDefinition
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.mbf.MBFBinaryReader
import de.bixilon.minosoft.util.json.Jackson
import de.matthiasmann.twl.utils.PNGDecoder
import org.kamranzafar.jtar.TarInputStream
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.util.zip.ZipInputStream

object InputStreamUtil {

    fun InputStream.readAsString(close: Boolean = true): String {
        val builder = StringBuilder()

        val buffer = ByteArray(BufferDefinition.DEFAULT_BUFFER_SIZE)
        var length: Int

        while (true) {
            length = this.read(buffer, 0, buffer.size)
            if (length < 0) {
                break
            }
            builder.append(String(buffer, 0, length, Charsets.UTF_8))
        }
        if (close) {
            this.close()
        }

        return builder.toString()
    }

    fun InputStream.readJsonObject(close: Boolean = true): JsonObject {
        try {
            return Jackson.MAPPER.readValue(this, Jackson.JSON_MAP_TYPE)
        } finally {
            if (close) {
                this.close()
            }
        }
    }

    inline fun <reified T> InputStream.readJson(close: Boolean = true): T {
        try {
            return Jackson.MAPPER.readValue(this)
        } finally {
            if (close) {
                this.close()
            }
        }
    }

    inline fun <reified T> InputStream.readJson(close: Boolean = true, type: JavaType): T {
        try {
            return Jackson.MAPPER.readValue(this, type)
        } finally {
            if (close) {
                this.close()
            }
        }
    }

    inline fun <reified T> InputStream.readJson(close: Boolean = true, reader: ObjectReader): T {
        try {
            return reader.readValue(this)
        } finally {
            if (close) {
                this.close()
            }
        }
    }

    fun InputStream.readArchive(): Map<String, ByteArray> {
        val content: MutableMap<String, ByteArray> = mutableMapOf()
        val stream = TarInputStream(this)
        while (true) {
            val entry = stream.nextEntry ?: break
            content[entry.name] = stream.readAllBytes()

        }
        return content
    }

    fun InputStream.readZipArchive(): Map<String, ByteArray> {
        val content: MutableMap<String, ByteArray> = mutableMapOf()
        val stream = ZipInputStream(this)
        while (true) {
            val entry = stream.nextEntry ?: break
            content[entry.name] = stream.readAllBytes()

        }
        return content
    }

    fun InputStream.readRGBArray(): IntArray {
        val decoder = PNGDecoder(this)

        val buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGB.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGB.numComponents, PNGDecoder.Format.RGB)
        buffer.flip()
        val colors = IntArray(decoder.width * decoder.height)

        for (i in colors.indices) {
            colors[i] = ((buffer.get().toInt() and 0xFF) shl 16) or ((buffer.get().toInt() and 0xFF) shl 8) or (buffer.get().toInt() and 0xFF)
        }

        return colors
    }

    fun InputStream.readMBFMap(): Map<Any, Any> {
        return MBFBinaryReader(this).readMBF().data.unsafeCast()
    }
}
