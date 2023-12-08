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

package de.bixilon.minosoft.gui.rendering.tint.tints.grass

import de.bixilon.minosoft.assets.MemoryAssetsManager
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["tint"])
class GrassTintCalculatorTest {
    private val map by lazy {
        val map = GrassTintCalculator()
        val assets = MemoryAssetsManager()
        assets.push(GrassTintCalculator.FILE, GrassTintCalculatorTest::class.java.getResourceAsStream("/tint/grass.png")!!.readAllBytes())
        map.init(assets)

        return@lazy map
    }


    fun `color of dessert`() {
        assertEquals(map.getColor(255, 0), 0xBFB755)
    }
}
