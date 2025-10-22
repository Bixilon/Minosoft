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

package de.bixilon.minosoft.data.container.stack.properties

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.registries.enchantment.tool.ToolEnchantment
import de.bixilon.minosoft.data.registries.enchantment.tool.weapon.WeaponEnchantment
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["item_stack"], dependsOnGroups = ["item"])
class EnchantingPropertyTest {

    fun `modern enchantments`() {
        val nbt: MutableJsonObject = mutableMapOf("Enchantments" to listOf(
            mapOf("id" to "minecraft:sharpness", "lvl" to 3.toShort()),
            mapOf("id" to "minecraft:unbreaking", "lvl" to 4.toShort()),
        ))

        val property = EnchantingProperty.of(IT.REGISTRIES.enchantment, nbt)
        val expected = EnchantingProperty(mapOf(WeaponEnchantment.Sharpness to 3, ToolEnchantment.Unbreaking to 4))

        assertEquals(property, expected)
        assertEquals(nbt, emptyMap<String, Any>())
    }

    fun `pre flattening enchantments`() {
        val nbt: MutableJsonObject = mutableMapOf("ench" to listOf(
            mapOf("id" to 16, "lvl" to 3.toShort()),
            mapOf("id" to 34, "lvl" to 4.toShort()),
        ))

        val property = EnchantingProperty.of(IT.REGISTRIES_PRE_FLATTENING.enchantment, nbt)
        val expected = EnchantingProperty(mapOf(WeaponEnchantment.Sharpness to 3, ToolEnchantment.Unbreaking to 4))

        assertEquals(property, expected)
        assertEquals(nbt, emptyMap<String, Any>())
    }
}
