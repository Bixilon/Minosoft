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

package de.bixilon.minosoft.data.registries.item.items.tool

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.properties.MiningSpeedTool
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.TagsS2CP

abstract class ToolItem(identifier: ResourceLocation) : Item(identifier), MiningTool, DurableItem {
    open val tag: ResourceLocation? get() = null

    @Deprecated("This was removed in 21w19a")
    protected open val mineable: Set<Block>? = null // TODO: remove, this is legacy and used for blocks that are not implemented
    override val maxStackSize: Int get() = 1


    protected open fun checkTag(connection: PlayConnection, blockState: BlockState): Boolean? {
        val blockTags = connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION] ?: return null
        val tag = blockTags[tag]?.entries ?: return null
        if (blockState.block !in tag) {
            return false
        }
        return true
    }

    private fun isEffectiveTool(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Boolean {
        if (blockState.block !is ToolRequirement) {
            // everything is effective, so …
            return true
        }
        return blockState.block.isCorrectTool(this)
    }

    override fun getMiningSpeed(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Float? {
        if (!isEffectiveTool(connection, blockState, stack)) {
            return null
        }
        if (this is MiningSpeedTool) {
            return this.miningSpeed
        }
        return 1.0f
    }

    companion object {
        fun Collection<Any?>.blocks(registries: Registries): Set<Block>? {
            if (this.isEmpty()) return null
            val set: MutableSet<Block> = mutableSetOf()

            for (entry in this) {
                set += registries.block[entry] ?: continue
            }

            return set
        }
    }
}
