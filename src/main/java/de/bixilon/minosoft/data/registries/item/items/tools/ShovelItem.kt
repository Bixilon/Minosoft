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

package de.bixilon.minosoft.data.registries.item.items.tools

import de.bixilon.kutil.cast.CollectionCast.toAnyMap
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class ShovelItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : MiningToolItem(resourceLocation, registries, data) {
    override val diggableTag: ResourceLocation = SHOVEL_MINEABLE_TAG
    val flattenableBlockStates: Map<Block, BlockState>? = data["flattenables_block_states"]?.toAnyMap()?.let {
        val entries: MutableMap<Block, BlockState> = mutableMapOf()
        for ((origin, target) in it) {
            entries[registries.block[origin.toInt()]] = registries.blockState[target]!!
        }
        entries
    }


    override fun interactBlock(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        if (!connection.profiles.controls.interaction.flattening) {
            return InteractionResults.CONSUME
        }

        if (connection.world[target.blockPosition + Directions.UP] != null) {
            return InteractionResults.PASS
        }

        return super.interactWithTool(connection, target.blockPosition, flattenableBlockStates?.get(target.blockState.block))
    }


    companion object : PixLyzerItemFactory<ShovelItem> {
        val SHOVEL_MINEABLE_TAG = "minecraft:mineable/shovel".toResourceLocation()

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): ShovelItem {
            return ShovelItem(resourceLocation, registries, data)
        }
    }
}
