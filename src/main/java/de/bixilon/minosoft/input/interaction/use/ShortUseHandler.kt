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

package de.bixilon.minosoft.input.interaction.use

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.camera.target.targets.GenericTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.handler.ItemInteractBlockHandler
import de.bixilon.minosoft.data.registries.item.handler.item.ItemUseHandler
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ShortUseHandler(
    private val interactionHandler: UseHandler,
) {
    private val connection = interactionHandler.connection


    fun interactBlock(target: BlockTarget, stack: ItemStack?, hand: Hands): InteractionResults {
        if (target.distance >= connection.player.reachDistance) {
            return InteractionResults.IGNORED
        }
        if (connection.world.border.isOutside(target.blockPosition)) {
            return InteractionResults.FAILED
        }
        // if out of world: return FAILED

        if (connection.player.gamemode == Gamemodes.SPECTATOR) {
            return InteractionResults.SUCCESS
        }

        var result = target.state.block.onUse(connection, target, hand, stack)
        if (result != InteractionResults.IGNORED) {
            return result
        }

        if (stack == null) {
            return InteractionResults.IGNORED
        }
        if (interactionHandler.interactions.isCoolingDown(stack.item.item)) {
            return InteractionResults.IGNORED // ToDo: Check
        }
        val item = stack.item.item
        if (item is ItemInteractBlockHandler) {
            result = item.interactBlock(connection.player, target, hand, stack)
            return result
        }

        return InteractionResults.IGNORED
    }

    private fun tryUse(hand: Hands, target: BlockTarget, stack: ItemStack?): Boolean {
        if (!connection.world.isValidPosition(target.blockPosition)) {
            return true
        }

        val copy = stack?.copy()

        val result = interactBlock(target, stack, hand)

        if (result == InteractionResults.INVALID) {
            return true
        }
        connection.sendPacket(BlockInteractC2SP(
            position = target.blockPosition,
            direction = target.direction,
            cursorPosition = Vec3(target.cursor),
            item = copy,
            hand = hand,
            insideBlock = target.inside,
            sequence = connection.sequence.getAndIncrement()
        ))

        if (result == InteractionResults.SUCCESS) {
            interactionHandler.interactions.swingHand(hand)
            return true
        }
        if (result == InteractionResults.IGNORED) {
            return false
        }
        return true
    }

    fun interactEntityAt(target: EntityTarget, hand: Hands, stack: ItemStack?): InteractionResults {
        val entityId = connection.world.entities.getId(target.entity) ?: return InteractionResults.IGNORED
        // used in armor stands
        val player = connection.player
        connection.sendPacket(EntityInteractPositionC2SP(entityId, Vec3(target.position), hand, player.isSneaking))

        if (player.gamemode == Gamemodes.SPECTATOR) {
            return InteractionResults.IGNORED
        }
        // ToDo:  return hit.entity.interactAt(hit.position, hand)
        return InteractionResults.IGNORED
    }

    fun interactEntity(target: EntityTarget, hand: Hands, stack: ItemStack?): InteractionResults {
        val player = connection.player
        try {

            if (player.gamemode == Gamemodes.SPECTATOR) {
                return InteractionResults.IGNORED
            }

            // ToDo: return hit.entity.interact(hand) (e.g. equipping saddle)
            return InteractionResults.IGNORED
        } finally {
            connection.world.entities.getId(target.entity)?.let { connection.sendPacket(EntityEmptyInteractC2SP(it, hand, player.isSneaking)) }
        }
    }

    private fun tryUse(hand: Hands, target: EntityTarget, stack: ItemStack?): Boolean {
        var result = interactEntityAt(target, hand, stack)

        if (result == InteractionResults.IGNORED) {
            result = interactEntity(target, hand, stack)
        }

        if (result == InteractionResults.SUCCESS) {
            interactionHandler.interactions.swingHand(hand)
            return true
        }
        if (result == InteractionResults.FAILED) {
            return true
        }
        return false
    }

    fun tryUse(hand: Hands, target: GenericTarget, item: ItemStack?): Boolean {
        when (target) {
            is EntityTarget -> return tryUse(hand, target, item)
            is BlockTarget -> return tryUse(hand, target, item)
        }

        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Can not handle target: $target" }
        return false
    }

    fun tryUse(hand: Hands, stack: ItemStack): Boolean {
        val item = stack.item.item

        if (item !is ItemUseHandler) {
            return false
        }

        val result = item.useItem(connection.player, hand, stack)
        if (result == InteractionResults.INVALID) {
            return true
        }
        interactionHandler.sendItemUse(hand, stack)
        if (result == InteractionResults.SUCCESS) {
            interactionHandler.interactions.swingHand(hand)
            return true
        }
        if (result == InteractionResults.FAILED) {
            return true
        }
        return false
    }
}
