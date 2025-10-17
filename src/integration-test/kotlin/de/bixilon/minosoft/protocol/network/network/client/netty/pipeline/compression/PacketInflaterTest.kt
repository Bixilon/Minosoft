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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression

import de.bixilon.kutil.compression.zlib.ZlibUtil.compress
import de.bixilon.kutil.buffer.bytes.ArbitraryByteBuffer
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketInflaterTest {

    private fun ArbitraryByteBuffer.decode(): ArbitraryByteBuffer {
        val inflater = PacketInflater(0xFF)
        return inflater.decode(this)
    }

    fun `uncompressed packet without data`() {
        val data = ArbitraryByteBuffer(0, 2, byteArrayOf(0, 1))
        val decoded = data.decode()

        assertSame(decoded.buffer, data.buffer)
        assertEquals(decoded.size, 1)
        assertEquals(decoded.offset, 1)
    }

    fun `uncompressed packet with data`() {
        val data = ArbitraryByteBuffer(0, 4, byteArrayOf(0, 1, 2, 3))
        val decoded = data.decode()

        assertEquals(decoded.size, 3)
        assertEquals(decoded.offset, 1)
    }

    fun `uncompressed packet with offset`() {
        val data = ArbitraryByteBuffer(2, 2, byteArrayOf(5, 1, 0, 3, 4))
        val decoded = data.decode()

        assertEquals(decoded.size, 1)
        assertEquals(decoded.offset, 3)
    }

    fun `compressed single byte`() {
        val compressed = byteArrayOf(0x01).compress()
        val data = ArbitraryByteBuffer(0, 1 + compressed.size, byteArrayOf(0x01) + compressed)
        val decoded = data.decode()

        assertNotSame(decoded.buffer, data.buffer)
        assertEquals(decoded.size, 1)
        assertEquals(decoded.offset, 0)
    }

    fun `compressed single byte offset`() {
        val compressed = byteArrayOf(0x01).compress()
        val data = ArbitraryByteBuffer(2, 1 + compressed.size, byteArrayOf(0x7A, 0x7B, 0x01) + compressed + byteArrayOf(0x7C, 0x7D))
        val decoded = data.decode()

        assertNotSame(decoded.buffer, data.buffer)
        assertEquals(decoded.size, 1)
        assertEquals(decoded.offset, 0)
    }
}
