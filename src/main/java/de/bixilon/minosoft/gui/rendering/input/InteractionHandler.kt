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

package de.bixilon.minosoft.gui.rendering.input

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.packets.c2s.play.ArmSwingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockBreakC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockPlaceC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.ItemUseC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i

class InteractionHandler(
    val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private var lastInteraction = 0L
    private var lastInteractionSent = 0L

    private var lastBreak = 0L
    private var lastBreakSent = 0L
    private var currentlyBreakingBlock: Vec3i? = null

    fun init() {
        renderWindow.inputHandler.registerCheckCallback(KeyBindingsNames.BLOCK_INTERACT)
        renderWindow.inputHandler.registerCheckCallback(KeyBindingsNames.DESTROY_BLOCK)
    }

    private fun checkBreaking(isKeyDown: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (!isKeyDown) {
            lastBreak = 0L
            return
        }
        if (currentTime - lastBreak < ProtocolDefinition.TICK_TIME * 5) {
            return
        }
        val raycastHit = renderWindow.inputHandler.camera.getTargetBlock()

        fun cancel() {
            currentlyBreakingBlock?.let {
                connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.CANCELLED_DIGGING, currentlyBreakingBlock, Directions.UP)) // ToDo
                currentlyBreakingBlock = null
            }
        }

        if (raycastHit?.blockPosition != currentlyBreakingBlock) {
            cancel()
        }
        if ((raycastHit?.distance ?: Float.MAX_VALUE) > RenderConstants.MAX_BLOCK_OUTLINE_RAYCAST_DISTANCE) {
            cancel()
            return
        }
        raycastHit ?: return

        if (currentTime - lastBreakSent < ProtocolDefinition.TICK_TIME) {
            return
        }

        if (!connection.player.entity.gamemode.canBreak) {
            return
        }

        currentlyBreakingBlock = raycastHit.blockPosition
        connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.START_DIGGING, raycastHit.blockPosition, raycastHit.hitDirection))
        connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.FINISHED_DIGGING, raycastHit.blockPosition, raycastHit.hitDirection))
        connection.world.setBlockState(raycastHit.blockPosition, null)
        currentlyBreakingBlock = null

        lastBreak = currentTime
        lastBreakSent = currentTime
    }

    private fun checkInteraction(isKeyDown: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (!isKeyDown) {
            lastInteraction = 0L
            return
        }
        if (currentTime - lastInteraction < ProtocolDefinition.TICK_TIME * 5) {
            return
        }

        val raycastHit = renderWindow.inputHandler.camera.getTargetBlock() ?: return

        if (raycastHit.distance > RenderConstants.MAX_BLOCK_OUTLINE_RAYCAST_DISTANCE) {
            return
        }
        val itemInHand = connection.player.inventory.getHotbarSlot()

        val usage = if (renderWindow.inputHandler.camera.sneaking) {
            BlockUsages.PASS
        } else {
            raycastHit.blockState.block.use(renderWindow.connection, raycastHit.blockState, raycastHit.blockPosition, raycastHit, Hands.MAIN_HAND, itemInHand)
        }

        lastInteractionSent = currentTime
        lastInteraction = currentTime

        when (usage) {
            BlockUsages.SUCCESS -> {
                if (currentTime - lastInteractionSent < ProtocolDefinition.TICK_TIME) {
                    return
                }
                connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))

                connection.sendPacket(BlockPlaceC2SP(
                    position = raycastHit.blockPosition,
                    direction = raycastHit.hitDirection,
                    cursorPosition = raycastHit.hitPosition,
                    item = connection.player.inventory.getHotbarSlot(),
                    hand = Hands.MAIN_HAND,
                    insideBlock = false,  // ToDo
                ))
            }
            BlockUsages.PASS -> {
                // use item or place block
                itemInHand ?: return

                val cooldown = connection.player.itemCooldown[itemInHand.item]

                cooldown?.let {
                    if (it.ended) {
                        connection.player.itemCooldown.remove(itemInHand.item)
                    } else {
                        return
                    }
                }



                when (itemInHand.item.use(connection, raycastHit.blockState, raycastHit.blockPosition, raycastHit, Hands.MAIN_HAND, itemInHand)) {
                    BlockUsages.SUCCESS -> {
                        connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))
                    }
                    BlockUsages.PASS -> {
                        return
                    }
                    BlockUsages.CONSUME -> {
                    }
                }
                // ToDo: Before 1.9
                connection.sendPacket(ItemUseC2SP(Hands.MAIN_HAND))
            }
        }

    }

    fun draw() {
        checkInteraction(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.BLOCK_INTERACT))
        checkBreaking(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.DESTROY_BLOCK))
    }
}
