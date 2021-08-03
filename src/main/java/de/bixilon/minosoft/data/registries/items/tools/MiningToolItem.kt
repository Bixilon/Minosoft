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
import de.bixilon.minosoft.data.registries.blocks.BlockUsages
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.TagsS2CP
import de.bixilon.minosoft.util.KUtil.listCast
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec3.Vec3i

abstract class MiningToolItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : ToolItem(resourceLocation, registries, data) {
    val diggableBlocks: Set<Block>? = data["diggable_blocks"]?.listCast()?.let {
        val entries: MutableList<Block> = mutableListOf()
        for (id in it) {
            entries += registries.blockRegistry[id]!!
        }
        entries.toSet()
    }
    override val attackDamage: Float = data["attack_damage"]?.unsafeCast<Float>() ?: 1.0f

    abstract val diggableTag: ResourceLocation?


    open fun isEffectiveOn(connection: PlayConnection, blockState: BlockState): Boolean {
        val blockTags = connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION] ?: mutableMapOf()
        return when {
            miningLevel < DIAMOND_MINING_LEVEL && blockTags[NEED_DIAMOND_TOOL_TAG]?.entries?.contains(blockState.block) == true -> false
            miningLevel < IRON_MINING_LEVEL && blockTags[NEED_IRON_TOOL_TAG]?.entries?.contains(blockState.block) == true -> false
            miningLevel < STONE_MINING_LEVEL && blockTags[NEED_STONE_TOOL_TAG]?.entries?.contains(blockState.block) == true -> false
            else -> blockTags[diggableTag]?.entries?.contains(blockState.block) == true || diggableBlocks?.contains(blockState.block) == true
        }
    }

    protected fun interactWithTool(connection: PlayConnection, blockPosition: Vec3i, replace: BlockState?): BlockUsages {
        if (!connection.player.gamemode.useTools) {
            return BlockUsages.PASS
        }

        replace ?: return BlockUsages.PASS


        connection.world[blockPosition] = replace
        return BlockUsages.SUCCESS
    }


    override fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, itemStack: ItemStack): Float {
        if (isEffectiveOn(connection, blockState)) {
            return speed
        }
        return super.getMiningSpeedMultiplier(connection, blockState, itemStack)
    }

    companion object {
        const val DIAMOND_MINING_LEVEL = 3
        const val IRON_MINING_LEVEL = 2
        const val STONE_MINING_LEVEL = 1

        val NEED_DIAMOND_TOOL_TAG = "minecraft:needs_diamond_tool".toResourceLocation()
        val NEED_IRON_TOOL_TAG = "minecraft:needs_iron_tool".toResourceLocation()
        val NEED_STONE_TOOL_TAG = "minecraft:needs_stone_tool".toResourceLocation()
    }

}
