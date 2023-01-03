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

package de.bixilon.minosoft.data.registries.integrated

import de.bixilon.minosoft.data.registries.enchantment.tool.WeaponEnchantment
import de.bixilon.minosoft.data.registries.item.items.food.AppleItem
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["registry"])
class IntegratedRegistryIT {

    fun integratedSharpness() {
        val expected = WeaponEnchantment.Sharpness
        val current = IT.REGISTRIES.enchantmentRegistry["minecraft:sharpness".toResourceLocation()]
        Assert.assertSame(current, expected)
    }

    fun goldenApple() {
        val current = IT.REGISTRIES.itemRegistry["minecraft:golden_apple".toResourceLocation()]
        if (current !is AppleItem.GoldenAppleItem) {
            Assert.fail("Not an golden apple: $current")
        }
    }
}
