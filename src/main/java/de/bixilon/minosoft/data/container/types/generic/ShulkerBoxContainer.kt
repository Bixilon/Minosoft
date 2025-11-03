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

package de.bixilon.minosoft.data.container.types.generic

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.item.items.block.BlockItem
import de.bixilon.minosoft.data.registries.item.items.block.legacy.PixLyzerBlockItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession


class ShulkerBoxContainer(session: PlaySession, type: ContainerType, title: ChatComponent?, id: Int) : Generic9x3Container(session, type, title, id) {

    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId in 0 until rows * SLOTS_PER_ROW) {
            return ShulkerBoxSlotType
        }
        return super.getSlotType(slotId)
    }

    object ShulkerBoxSlotType : SlotType {

        override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
            if (stack.item is BlockItem<*> && stack.item.block is ShulkerBoxBlock) return false
            if (stack.item is PixLyzerBlockItem && stack.item.block is ShulkerBoxBlock) return false

            return stack.item.identifier != ShulkerBoxBlock.identifier // TODO: don't compare identifier
        }
    }

    companion object : ContainerFactory<ShulkerBoxContainer> {
        override val identifier = ShulkerBoxBlock.identifier

        override fun build(session: PlaySession, type: ContainerType, title: ChatComponent?, slots: Int, id: Int): ShulkerBoxContainer {
            return ShulkerBoxContainer(session, type, title, id)
        }
    }
}
