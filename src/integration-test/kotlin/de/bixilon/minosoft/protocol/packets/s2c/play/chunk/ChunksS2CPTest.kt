/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.s2c.play.chunk

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.stream.InputStreamUtil.readAll
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunksS2CP
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import org.testng.Assert.assertNotNull
import org.testng.annotations.Test

@Test(groups = ["packet"])
class ChunksS2CPTest {

    private fun read(name: String, version: String, session: PlaySession = createSession(version = version), dimension: DimensionProperties): ChunksS2CP {
        val data = ChunksS2CPTest::class.java.getResourceAsStream("/packets/chunks/$name.bin")!!.readAll()
        session.world::dimension.forceSet(DataObserver(dimension))

        val buffer = PlayInByteBuffer(data, session)
        val packet = ChunksS2CP(buffer)

        return packet
    }

    @Test(groups = ["packet"])
    fun vanilla_1_7_10() {
        val packet = read("vanilla_1_7_10", "1.7.10", dimension = DimensionProperties(light = true, skyLight = true, minY = 0, height = 256))

        assertNotNull(packet.chunks[ChunkPosition(0, 14)]?.blocks?.get(0))
        assertNotNull(packet.chunks[ChunkPosition(1, 14)]?.blocks?.get(0))
        assertNotNull(packet.chunks[ChunkPosition(1, 15)]?.blocks?.get(0))
        assertNotNull(packet.chunks[ChunkPosition(0, 15)]?.blocks?.get(0))
        assertNotNull(packet.chunks[ChunkPosition(-1, 15)]?.blocks?.get(0))
    }
}

