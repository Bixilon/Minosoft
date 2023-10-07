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

package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.dimension.effects.OverworldEffects
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["packet"])
class InitializeS2CPTest {

    fun vanilla_1_15_2() {
        val packet = PacketReadingTestUtil.read("initialize/vanilla_1_15_2", "1.15.2", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.SURVIVAL)
        assertEquals(packet.dimension?.effects, OverworldEffects)
        assertEquals(packet.entityId, 424)
        assertEquals(packet.viewDistance, 9)
    }

    fun vanilla_1_16_5() {
        val packet = PacketReadingTestUtil.read("initialize/vanilla_1_16_5", "1.16.5", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.CREATIVE)
        assertEquals(packet.dimension?.effects, OverworldEffects)
        assertEquals(packet.entityId, 321)
        assertEquals(packet.viewDistance, 8)
        assertEquals(packet.registries?.size, 2)
        assertTrue(packet.registries!!["minecraft:worldgen/biome"] is Map<*, *>)
    }

    fun hypixel_1_19_4() {
        val packet = PacketReadingTestUtil.read("initialize/hypixel_1_19_4", "1.19.4", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.SURVIVAL)
        assertEquals(packet.dimensionName, OverworldEffects.identifier)
        assertEquals(packet.entityId, 11659106)
        assertNull(packet.lastDeathPosition)
        assertEquals(packet.registries?.size, 6)
        assertTrue(packet.registries!!["minecraft:worldgen/biome"] is Map<*, *>)
    }

    fun vanilla_1_20_1() {
        val packet = PacketReadingTestUtil.read("initialize/vanilla_1_20_1", "1.20.1", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.CREATIVE)
        assertEquals(packet.dimensionName, OverworldEffects.identifier)
        assertNull(packet.lastDeathPosition)
        assertEquals(packet.registries?.size, 6)
        assertTrue(packet.registries!!["minecraft:worldgen/biome"] is Map<*, *>)
    }

    fun vanilla_1_20_2() {
        val packet = PacketReadingTestUtil.read("initialize/vanilla_1_20_2", "1.20.2", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.SURVIVAL)
        assertEquals(packet.dimensionName, OverworldEffects.identifier)
        assertNull(packet.registries)
    }

    fun vanilla_1_7_10() {
        val packet = PacketReadingTestUtil.read("initialize/vanilla_1_7_10", "1.7.10", constructor = ::InitializeS2CP)
        assertEquals(packet.gamemode, Gamemodes.CREATIVE)
        assertEquals(packet.dimension?.effects, OverworldEffects.identifier)
        assertNull(packet.registries)
    }
}

