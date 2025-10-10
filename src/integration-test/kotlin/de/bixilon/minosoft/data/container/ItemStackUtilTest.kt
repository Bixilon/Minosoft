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

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.stack.properties.DisplayProperty
import de.bixilon.minosoft.data.container.stack.properties.DurabilityProperty
import de.bixilon.minosoft.data.container.stack.properties.EnchantingProperty
import de.bixilon.minosoft.data.registries.enchantment.tool.ToolEnchantment
import de.bixilon.minosoft.data.registries.enchantment.tool.weapon.WeaponEnchantment
import de.bixilon.minosoft.data.registries.items.AppleTest0
import de.bixilon.minosoft.data.text.TextComponent
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test

@Test(groups = ["item_stack"], dependsOnGroups = ["item"])
class ItemStackUtilTest {

    fun `multiple enchantments`() { // TODO: pre flattening
        val nbt = mapOf("Enchantments" to listOf(
            mapOf("id" to "minecraft:sharpness", "lvl" to 3.toShort()),
            mapOf("id" to "minecraft:durability", "lvl" to 4.toShort()),
        ))

        val stack = ItemStackUtil.of(AppleTest0.item, nbt = nbt)
        val expected = ItemStack(AppleTest0.item, enchanting = EnchantingProperty(mapOf(WeaponEnchantment.Sharpness to 3, ToolEnchantment.Durability to 4)))

        assertEquals(stack, expected)
    }

    fun `display properties`() {
        val nbt = mapOf("display" to mapOf(
            "Name" to "display name",
            "Lore" to listOf(
                "first line",
                "second line",
            ),
        ))

        val stack = ItemStackUtil.of(AppleTest0.item, nbt = nbt)
        val expected = ItemStack(AppleTest0.item, display = DisplayProperty(listOf(TextComponent("first line"), TextComponent("second line")), customDisplayName = TextComponent("display name")))

        assertEquals(stack, expected)
    }
    // TODO: dye color

    fun `unbreakable property`() {
        val nbt = mapOf("unbreakable" to 1.toByte())

        val stack = ItemStackUtil.of(AppleTest0.item, nbt = nbt)
        val expected = ItemStack(AppleTest0.item, durability = DurabilityProperty(unbreakable = true))

        assertEquals(stack, expected)
    }

    // TODO: HideFlags


    // TODO: hide, nbt, serialize
}
