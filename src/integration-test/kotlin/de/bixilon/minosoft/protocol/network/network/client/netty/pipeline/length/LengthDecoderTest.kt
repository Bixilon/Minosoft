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

import io.netty.buffer.Unpooled
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["network"])
class LengthDecoderTest {

    private fun ByteArray.decode(): LengthDecodedPacket? {
        val decoder = LengthDecoder(1 shl 16)
        return decoder.read(Unpooled.wrappedBuffer(this))
    }

    fun `no data`() {
        val data = byteArrayOf()
        assertEquals(data.decode(), null)
    }

    fun `just one byte`() {
        val data = byteArrayOf(0x01)
        assertEquals(data.decode(), null)
    }

    fun `one byte length data`() {
        val data = byteArrayOf(0x01, 0x22)
        val decoded = data.decode()!!

        assertEquals(decoded.size, 1)
    }

    fun `two bytes length data`() {
        val data = byteArrayOf(0x02, 0x22, 0x33)
        val decoded = data.decode()!!

        assertEquals(decoded.size, 2)
    }

    fun `two bytes length data, but not enough data`() {
        val data = byteArrayOf(0x02, 0x22)

        assertEquals(data.decode(), null)
    }

    fun `128 bytes of data`() {
        val data = byteArrayOf(0x80.toByte(), 0x01) + ByteArray(128) + ByteArray(5) // trailing
        val decoded = data.decode()!!

        assertEquals(decoded.size, 128)
    }
}
