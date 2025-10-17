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
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyTestUtil.toArray
import de.bixilon.kutil.buffer.bytes.ArbitraryByteBuffer
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketDeflaterTest {

    private fun ArbitraryByteBuffer.encode(threshold: Int = 3): ArbitraryByteBuffer {
        val inflater = PacketDeflater(threshold)
        return inflater.encode(this)
    }

    private fun assertEquals(actual: ArbitraryByteBuffer, expected: ByteArray) {
        Assert.assertEquals(actual.toArray(), expected)
    }

    fun `below threshold, single byte data`() {
        val data = ArbitraryByteBuffer(0, 1, byteArrayOf(0x11))
        val decoded = data.encode()

        assertEquals(decoded, byteArrayOf(0x00, 0x11))
    }

    fun `below threshold, single byte data with offset and trailing data`() {
        val data = ArbitraryByteBuffer(2, 1, ByteArray(2) + byteArrayOf(0x11) + ByteArray(3))
        val decoded = data.encode()

        assertEquals(decoded, byteArrayOf(0x00, 0x11))
    }

    fun `above threshold, single byte data`() {
        val data = ArbitraryByteBuffer(0, 1, byteArrayOf(0x11))
        val compressed = byteArrayOf(0x11).compress()
        val decoded = data.encode(0x00)

        assertEquals(decoded, byteArrayOf(0x01) + compressed)
    }

    fun `above threshold, single byte data with offset and trailing data`() {
        val data = ArbitraryByteBuffer(2, 1, ByteArray(2) + byteArrayOf(0x11) + ByteArray(3))
        val compressed = byteArrayOf(0x11).compress()
        val decoded = data.encode(0x00)

        assertEquals(decoded, byteArrayOf(0x01) + compressed)
    }

    // TODO: exact threshold, more data
}
