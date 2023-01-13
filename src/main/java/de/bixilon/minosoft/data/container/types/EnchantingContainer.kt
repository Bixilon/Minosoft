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

package de.bixilon.minosoft.data.container.types

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.InventorySynchronizedContainer
import de.bixilon.minosoft.data.container.actions.types.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.sections.HotbarSection
import de.bixilon.minosoft.data.container.sections.PassiveInventorySection
import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.EnchantableSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.MinecraftItems
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerButtonC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class EnchantingContainer(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : InventorySynchronizedContainer(connection, type, title, RangeSection(ENCHANTING_SLOTS, PlayerInventory.MAIN_SLOTS)) {
    override val sections: Array<ContainerSection> get() = SECTIONS
    val costs = IntArray(ENCHANTING_OPTIONS) { -1 }
    val enchantments: Array<Enchantment?> = arrayOfNulls(ENCHANTING_OPTIONS)
    var enchantmentLevels = IntArray(ENCHANTING_OPTIONS) { -1 }
    var seed = -1
        private set

    val lapislazuli: Int get() = this[LAPISLAZULI_SLOT]?.item?._count ?: 0

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
            4, 5, 6 -> enchantments[property - 4] = connection.registries.enchantment.getOrNull(value)
            7, 8, 9 -> enchantmentLevels[property - 7] = value
        }
    }

    fun canEnchant(index: Int): Boolean {
        val cost = costs[index]
        if (cost < 0) {
            return false
        }
        if (connection.player.gamemode == Gamemodes.CREATIVE) {
            return true
        }
        if (connection.player.experienceCondition.level < cost) {
            return false
        }
        val lapislazuli = this.lapislazuli
        if (lapislazuli < index + 1) {
            return false
        }

        return true
    }


    fun selectEnchantment(index: Int) {
        if (index < 0 || index > 2) {
            throw IllegalArgumentException("Index out of bounds: $index")
        }
        if (!canEnchant(index)) {
            throw IllegalStateException("Can not enchant $index!")
        }
        val id = this.id ?: return
        connection.network.send(ContainerButtonC2SP(id, index))
    }

    private object LapislazuliSlot : SlotType {
        override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
            return stack.item.item.identifier == MinecraftItems.LAPISLAZULI
        }
    }


    companion object : ContainerFactory<EnchantingContainer>, AliasedIdentified {
        override val identifier: ResourceLocation = "minecraft:enchantment".toResourceLocation()
        override val identifiers: Set<ResourceLocation> = setOf("minecraft:enchanting_table".toResourceLocation(), "EnchantTable".toResourceLocation())
        const val LAPISLAZULI_SLOT = 1
        const val ENCHANTING_SLOTS = 2
        const val ENCHANTING_OPTIONS = 3


        private val SECTIONS: Array<ContainerSection> = arrayOf(
            RangeSection(0, ENCHANTING_SLOTS),
            HotbarSection(ENCHANTING_SLOTS + PlayerInventory.PASSIVE_SLOTS),
            PassiveInventorySection(ENCHANTING_SLOTS),
        )

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): EnchantingContainer {
            return EnchantingContainer(connection, type, title)
        }
    }
}
