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

import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection


open class SwordItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : ToolItem(resourceLocation, registries, data) {
    override val attackDamage = data["attack_damage"]?.toFloat() ?: -1.0f

    override fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Float {
        if (blockState.block.resourceLocation == MinecraftBlocks.COBWEB) {
            return 15.0f
        }
        return super.getMiningSpeedMultiplier(connection, blockState, stack)
    }
}
