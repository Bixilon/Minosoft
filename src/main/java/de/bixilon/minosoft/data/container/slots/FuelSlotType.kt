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

package de.bixilon.minosoft.data.container.slots

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.item.items.bucket.BucketItem

@Deprecated("ToDo")
object FuelSlotType : SlotType {

    override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
        val item = stack.item.item

        if (item is BucketItem && item.fluid.resourceLocation == DefaultFluids.LAVA) {
            return true
        }
        // ToDo: get from registries (misc/fuel_time)
        return super.canPut(container, slot, stack)
    }
}
