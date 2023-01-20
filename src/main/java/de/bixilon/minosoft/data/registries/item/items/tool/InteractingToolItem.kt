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

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class InteractingToolItem(identifier: ResourceLocation) : LeveledToolItem(identifier) {

    protected fun interact(connection: PlayConnection, blockPosition: BlockPosition, replace: BlockState?): InteractionResults {
        if (replace == null || !connection.player.gamemode.useTools) {
            return InteractionResults.PASS
        }

        connection.world[blockPosition] = replace
        return InteractionResults.SUCCESS
    }

    companion object {
        fun Map<Any, Any>.blocks(registries: Registries): Map<Block, Block>? {
            if (this.isEmpty()) return null
            val map: MutableMap<Block, Block> = mutableMapOf()

            for ((origin, target) in this) {
                map[registries.block[origin] ?: continue] = registries.block[target] ?: continue
            }

            return map
        }

        fun Map<Any, Any>.states(registries: Registries): Map<Block, BlockState>? {
            if (this.isEmpty()) return null
            val map: MutableMap<Block, BlockState> = mutableMapOf()

            for ((origin, target) in this) {
                map[registries.block[origin] ?: continue] = registries.blockState[target] ?: continue
            }

            return map
        }
    }
}
