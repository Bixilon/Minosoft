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

package de.bixilon.minosoft.data.container.types.generic

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection


class ShulkerBoxContainer(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : Generic9x3Container(connection, type, title) {

    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId in 0 until rows * SLOTS_PER_ROW) {
            return ShulkerBoxSlotType
        }
        return super.getSlotType(slotId)
    }

    private object ShulkerBoxSlotType : SlotType {

        override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
            return stack.item.item.identifier == identifier // TODO: don't compare identifier
        }
    }

    companion object : ContainerFactory<ShulkerBoxContainer> {
        override val identifier: ResourceLocation = minecraft("shulker_box")

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?, slots: Int): ShulkerBoxContainer {
            return ShulkerBoxContainer(connection, type, title)
        }
    }
}
