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

package de.bixilon.minosoft.gui.rendering.tint.tints

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["biome"])
class ColorMapTintTest {

    @Test
    fun `plains color map index`() {
        val biome = Biome(minecraft("plains"), temperature = 0.8f, downfall = 0.4f)
        assertEquals(biome.temperatureIndex, 50)
        assertEquals(biome.downfallIndex, 173)
    }

    @Test
    fun `dessert color map index`() {
        val biome = Biome(minecraft("dessert"), temperature = 2.0f, downfall = 0.0f)
        assertEquals(biome.temperatureIndex, 0)
        assertEquals(biome.downfallIndex, 255)
    }
}
