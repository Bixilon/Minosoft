/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.item.items.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.blocks.handler.entity.BlockPlaceHandler
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.ReplaceableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.item.handler.ItemInteractBlockHandler
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plusAssign
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

interface PlaceableItem : ItemInteractBlockHandler {

    fun canPlace(session: PlaySession, target: BlockTarget, stack: ItemStack): Boolean = true
    fun getPlacementState(session: PlaySession, target: BlockTarget, stack: ItemStack): BlockState


    fun place(player: LocalPlayerEntity, target: BlockTarget, stack: ItemStack): InteractionResults {
        if (!player.gamemode.canBuild) {
            return InteractionResults.INVALID
        }
        if (target.inside) {
            // TODO: scaffolding
            return InteractionResults.INVALID // This is not valid with vanilla. Vanilla just inverts the direction and places a block.
        }
        val session = player.session
        val world = session.world

        val placePosition = Vec3i(target.blockPosition)
        if (target.state.block !is ReplaceableBlock || !target.state.block.canReplace(session, target.state, target.blockPosition)) {
            placePosition += target.direction

            val targetBlock = session.world[placePosition]
            if (targetBlock != null && (targetBlock.block !is ReplaceableBlock || !targetBlock.block.canReplace(session, targetBlock, placePosition))) {
                return InteractionResults.IGNORED
            }
        }


        if (!session.world.isPositionChangeable(placePosition)) {
            return InteractionResults.INVALID
        }

        if (world.getBlockEntity(placePosition) != null && !player.isSneaking) {
            return InteractionResults.IGNORED
        }


        val state: BlockState = getPlacementState(session, target, stack)
        if (state.block is CollidableBlock) {
            val shape = state.block.getCollisionShape(session, EntityCollisionContext(player), placePosition, state, null)?.plus(placePosition)
            if (shape != null && session.world.entities.isEntityIn(shape)) {
                return InteractionResults.INVALID
            }
        }


        if (player.gamemode != Gamemodes.CREATIVE) {
            stack.item.decreaseCount()
        }
        DefaultThreadPool += {
            world[placePosition] = state
            if (state.block is BlockPlaceHandler) {
                state.block.onPlace(session, placePosition, state, null) // TODO: block entity
            }
            // TODO: handle ReplaceableBlock::onDestroy
        }

        state.block.soundGroup?.let { group ->
            group.place?.let { world.playSoundEvent(it, placePosition, group.volume, group.pitch) }
        }
        return InteractionResults.SUCCESS
    }


    override fun interactBlock(player: LocalPlayerEntity, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return place(player, target, stack)
    }
}
