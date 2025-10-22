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

package de.bixilon.minosoft.data.registries.item.items.tool

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.CustomDiggingBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.properties.MiningSpeedTool
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.tags.MinecraftTagTypes
import de.bixilon.minosoft.tags.TagManager

abstract class ToolItem(identifier: ResourceLocation) : Item(identifier), MiningTool, DurableItem {
    open val tag: ResourceLocation? get() = null


    private fun isInTag(tagManager: TagManager, blockState: BlockState): Boolean? {
        val miningTag = this.tag ?: return null
        val blockTags = tagManager[MinecraftTagTypes.BLOCK] ?: return null
        val tag = blockTags[miningTag] ?: return null
        if (blockState.block !in tag) {
            return false
        }
        return true
    }

    private fun isInTag(session: PlaySession, blockState: BlockState): Boolean? {
        return isInTag(session.tags, blockState) ?: isInTag(session.legacyTags, blockState)
    }

    protected open fun isLevelSuitable(session: PlaySession, blockState: BlockState): Boolean? {
        return isInTag(session, blockState)
    }

    override fun isSuitableFor(session: PlaySession, state: BlockState, stack: ItemStack): Boolean {
        isLevelSuitable(session, state)?.let { if (it) return true }
        if (state.block !is ToolRequirement) {
            // everything is effective, so …
            return true
        }
        return state.block.isCorrectTool(this)
    }

    override fun getMiningSpeed(session: PlaySession, state: BlockState, stack: ItemStack): Float {
        var speed = 1.0f
        if (this is MiningSpeedTool) {
            speed = this.miningSpeed
        }

        if (state.block is CustomDiggingBlock) {
            speed = state.block.getMiningSpeed(session, state, stack, speed)
        }
        isInTag(session, state)?.let { if (it) return speed else 1.0f }

        if (state.block !is ToolRequirement) return speed

        if (!state.block.isCorrectTool(stack.item)) return 1.0f

        return speed
    }
}
