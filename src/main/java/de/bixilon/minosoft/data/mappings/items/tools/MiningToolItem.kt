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

package de.bixilon.minosoft.data.mappings.items.tools

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.blocks.types.Block
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class MiningToolItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : ToolItem(resourceLocation, registries, data) {
    val diggableBlocks: Set<Block>? = data["diggable_blocks"]?.asJsonArray?.let {
        val entries: MutableList<Block> = mutableListOf()
        for (id in it) {
            entries += registries.blockRegistry[id]
        }
        entries.toSet()
    }
    override val attackDamage: Float = data["attack_damage"]?.asFloat ?: 1.0f


    open fun isEffectiveOn(blockState: BlockState): Boolean {
        return diggableBlocks?.contains(blockState.block) == true
    }

    protected fun interactWithTool(connection: PlayConnection, blockPosition: Vec3i, replace: BlockState?): BlockUsages {
        if (!connection.player.entity.gamemode.useTools) {
            return BlockUsages.PASS
        }

        replace ?: return BlockUsages.PASS


        connection.world[blockPosition] = replace
        return BlockUsages.SUCCESS
    }

    override fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, itemStack: ItemStack): Float {
        // ToDo: Calculate correct, Tags (21w19a)
        if (isEffectiveOn(blockState)) {
            return speed
        }
        return super.getMiningSpeedMultiplier(connection, blockState, itemStack)
    }

}
