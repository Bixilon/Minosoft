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

package de.bixilon.minosoft.data.registries.item.items.fire

import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.LitBlock
import de.bixilon.minosoft.data.registries.item.handler.ItemInteractBlockHandler
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

interface FireItem : ItemInteractBlockHandler {

    override fun interactBlock(player: LocalPlayerEntity, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        if (target.state.block is LitBlock) {
            if (this is DurableItem && player.gamemode != Gamemodes.CREATIVE) {
                stack.decreaseCount()
            }
            return if (target.state.block.light(player.session, target.blockPosition, target.state)) InteractionResults.SUCCESS else InteractionResults.FAILED
        }
        val placed = placeFireAt(player.session, target.blockPosition)

        return if (placed) InteractionResults.SUCCESS else InteractionResults.FAILED
    }

    fun placeFireAt(session: PlaySession, position: BlockPosition): Boolean {
        return true // TODO
    }
}
