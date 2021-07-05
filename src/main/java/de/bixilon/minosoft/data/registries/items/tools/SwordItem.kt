/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.items.tools

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.DefaultBlocks
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast


open class SwordItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : ToolItem(resourceLocation, registries, data) {
    override val attackDamage = data["attack_damage"]?.nullCast<Float>() ?: -1.0f

    override fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, itemStack: ItemStack): Float {
        if (blockState.block.resourceLocation == DefaultBlocks.COBWEB) {
            return 15.0f
        }
        return super.getMiningSpeedMultiplier(connection, blockState, itemStack)
    }
}
