/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.types

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.ContainerUtil.section
import de.bixilon.minosoft.data.container.InventorySynchronizedContainer
import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.EnchantableSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.MultiResourceLocationAble
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.item.MinecraftItems
import de.bixilon.minosoft.data.registries.other.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class EnchantingContainer(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : InventorySynchronizedContainer(connection, type, title, section(ENCHANTING_SLOTS, PlayerInventory.MAIN_SLOTS)) {
    override val sections: Array<IntRange> get() = SECTIONS
    val costs = IntArray(ENCHANTING_OPTIONS) { -1 }
    val enchantments: Array<Enchantment?> = arrayOfNulls(ENCHANTING_OPTIONS)
    var enchantmentLevels = IntArray(ENCHANTING_OPTIONS) { -1 }
    var seed = -1
        private set

    override fun getSlotType(slotId: Int): SlotType? {
        return when (slotId) {
            0 -> EnchantableSlotType
            1 -> LapislazuliSlot
            in ENCHANTING_SLOTS until ENCHANTING_SLOTS + PlayerInventory.MAIN_SLOTS -> DefaultSlotType
            else -> null
        }
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return null // ToDo: It is possible to press F in vanilla, but there is no slot for it
        }
        return 1 + ENCHANTING_SLOTS + PlayerInventory.MAIN_SLOTS_PER_ROW * 3 + slot.ordinal
    }

    override fun readProperty(property: Int, value: Int) {
        when (property) {
            0, 1, 2 -> costs[property] = value
            3 -> seed = value
            4, 5, 6 -> enchantments[property - 4] = connection.registries.enchantmentRegistry.getOrNull(value)
            7, 8, 9 -> enchantmentLevels[property - 7] = value
        }
    }

    private object LapislazuliSlot : SlotType {
        override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
            return stack.item.item.resourceLocation == MinecraftItems.LAPISLAZULI
        }
    }


    companion object : ContainerFactory<EnchantingContainer>, MultiResourceLocationAble {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:enchantment".toResourceLocation()
        override val ALIASES: Set<ResourceLocation> = setOf("minecraft:enchanting_table".toResourceLocation(), "EnchantTable".toResourceLocation())
        const val LAPISLAZULI_SLOT = 1
        const val ENCHANTING_SLOTS = 2
        const val ENCHANTING_OPTIONS = 3


        private val SECTIONS: Array<IntRange> = arrayOf(
            section(0, ENCHANTING_SLOTS),
            section(ENCHANTING_SLOTS + PlayerInventory.PASSIVE_SLOTS, PlayerInventory.HOTBAR_SLOTS),
            section(ENCHANTING_SLOTS, PlayerInventory.PASSIVE_SLOTS),
        )

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): EnchantingContainer {
            return EnchantingContainer(connection, type, title)
        }
    }
}
