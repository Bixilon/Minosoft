/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length

import de.bixilon.kutil.buffer.bytes.ArbitraryByteBuffer
import io.netty.buffer.Unpooled
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["network"])
class LengthEncoderTest {

    private fun ArbitraryByteBuffer.encode(): ByteArray {
        val buffer = Unpooled.buffer()
        LengthEncoder(1 shl 16).write(this, buffer)
        val array = ByteArray(buffer.writerIndex())
        buffer.readerIndex(0)
        buffer.readBytes(array)
        return array
    }

    fun `no data`() {
        val data = ArbitraryByteBuffer(0, 0, byteArrayOf())
        val expected = byteArrayOf(0x00)
        assertEquals(data.encode(), expected)
    }

    fun `single byte`() {
        val data = ArbitraryByteBuffer(0, 1, byteArrayOf(0x11))
        val expected = byteArrayOf(0x01, 0x11)
        assertEquals(data.encode(), expected)
    }

    fun `128 bytes`() {
        val data = ArbitraryByteBuffer(0, 128, ByteArray(128) { 0x11 })
        val expected = byteArrayOf(0x80.toByte(), 0x01) + ByteArray(128) { 0x11 }
        assertEquals(data.encode(), expected)
    }

    fun `offset and trailing data`() {
        val data = ArbitraryByteBuffer(5, 128, ByteArray(5) + ByteArray(128) { 0x11 } + ByteArray(8))
        val expected = byteArrayOf(0x80.toByte(), 0x01) + ByteArray(128) { 0x11 }
        assertEquals(data.encode(), expected)
    }
}
