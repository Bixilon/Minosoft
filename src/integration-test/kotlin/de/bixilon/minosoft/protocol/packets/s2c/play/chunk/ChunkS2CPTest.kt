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

package de.bixilon.minosoft.protocol.packets.s2c.play.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.mbf.MBFBinaryReader
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.stone.RockBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkS2CP
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["packet"])
class ChunkS2CPTest {

    private fun PlayConnection.readRegistries(name: String) {
        val steam = ChunkS2CPTest::class.java.getResourceAsStream("/packets/chunk/$name.mbf")
        val mbf = MBFBinaryReader(steam!!).readMBF()
        registries.update(version, mbf.data.unsafeCast())
    }

    private fun read(name: String, version: String, connection: PlayConnection = createConnection(version = version), dimension: DimensionProperties): ChunkS2CP {
        val data = ChunkS2CPTest::class.java.getResourceAsStream("/packets/chunk/$name.bin")!!.readAllBytes()
        connection.world::dimension.forceSet(DataObserver(dimension))

        val buffer = PlayInByteBuffer(data, connection)
        val packet = ChunkS2CP(buffer)
        packet.parse()

        return packet
    }

    // hypixel does some magic to their packets and it failed (22-02-05). This is used to test it
    fun hypixel1_19_3() {
        val connection = createConnection(version = "1.19.3")
        connection.readRegistries("hypixel_registries")
        val packet = read("hypixel_hub_1_19_3", "1.19.3", dimension = DimensionProperties(light = true, skyLight = true, minY = 0, height = 256))
        assertEquals(packet.position, Vec2i(-10, 9))
        val blocks = packet.prototype.blocks!!
        assertNull(blocks[0])
        assertNull(blocks[1])
        assertNotNull(blocks[2])
        assertNotNull(blocks[3])
        assertNotNull(blocks[4])
        assertNull(blocks[5])
        // rest is null

        assertEquals(blocks[2]!![0, 12, 7]?.block?.identifier, RockBlock.Stone.identifier)
        assertNull(blocks[2]!![1, 12, 7])
    }

    fun vanilla_1_19_3() {
        val packet = read("vanilla_1_19_3", "1.19.3", dimension = DimensionProperties(light = true, skyLight = true, minY = -64, height = 384))
        assertEquals(packet.position, Vec2i(-4, 13))
        val prototype = packet.prototype
        assertNotNull(prototype.blocks)
        assertEquals(packet.prototype.blocks!![4]!![0]!!.block.identifier, MinecraftBlocks.BEDROCK)
    }

    fun feather_1_16_5() {
        val packet = read("feather_1_16_5", "1.16.5", dimension = DimensionProperties(light = true, skyLight = true, minY = 0, height = 256))
        assertEquals(packet.position, Vec2i(1, 0))
        val prototype = packet.prototype
        assertNotNull(prototype.blocks)
        assertEquals(packet.prototype.blocks!![0]!![0]!!.block.identifier, MinecraftBlocks.BEDROCK)
    }

    fun cuberite_1_8_9() {
        val packet = read("cuberite_1_8_9", "1.8.9", dimension = DimensionProperties(light = true, skyLight = true, minY = 0, height = 256))
        assertEquals(packet.position, Vec2i(0, 0))
        val blocks = packet.prototype.blocks
        assertNotNull(blocks); blocks!!
        assertEquals(blocks[0]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.BEDROCK)
        assertEquals(blocks[0]!![0, 1, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[1]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[2]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[3]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[3]!![0, 8, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[3]!![0, 11, 0]!!.block.identifier, MinecraftBlocks.DIRT)
    }

    @Test(groups = ["packet"])
    fun cuberite_1_12_2() {
        val packet = read("cuberite_1_12_2", "1.12.2", dimension = DimensionProperties(light = true, skyLight = true, minY = 0, height = 256))
        assertEquals(packet.position, Vec2i(0, 0))
        val blocks = packet.prototype.blocks
        assertNotNull(blocks); blocks!!
        assertNull(blocks[0]!![0, 0, 0])

        assertEquals(blocks[0]!![2, 0, 0]!!.block.identifier, MinecraftBlocks.BEDROCK)
        assertEquals(blocks[0]!![5, 5, 1]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[1]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[2]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[3]!![0, 0, 0]!!.block.identifier, MinecraftBlocks.STONE)
        assertEquals(blocks[3]!![1, 11, 0]!!.block.identifier, MinecraftBlocks.DIRT)
        assertEquals(blocks[4]!![4, 3, 4]!!.block.identifier, MinecraftBlocks.STONE)
    }
}

