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
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.items.BlockItem
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.packets.c2s.play.ArmSwingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockBreakC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockPlaceC2SP
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
        connection.world.setBlock(raycastHit.blockPosition, null)
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

        val usage = raycastHit.blockState.block.use(renderWindow.connection, raycastHit.blockState, raycastHit.blockPosition, raycastHit, Hands.MAIN_HAND, null) // ToDo

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
                if (!connection.player.entity.gamemode.canBuild) {
                    return
                }
                val selectedItemStack = connection.player.inventory.getHotbarSlot() ?: return
                val blockPosition = raycastHit.blockPosition + raycastHit.hitDirection

                val dimension = connection.world.dimension!!
                if (blockPosition.y < dimension.minY || blockPosition.y >= dimension.height) {
                    return
                }

                renderWindow.connection.world.getBlockState(blockPosition)?.let {
                    if (!it.material.replaceable) {
                        return
                    }
                }


                val blockState = if (selectedItemStack.item is BlockItem) {
                    selectedItemStack.item.block.getPlacementState(renderWindow.connection, raycastHit) ?: return
                } else {
                    return
                }

                renderWindow.connection.world.setBlock(blockPosition, blockState)

                if (connection.player.entity.gamemode != Gamemodes.CREATIVE) {
                    selectedItemStack.count--
                    renderWindow.connection.player.inventory.validate()
                }

                connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))

                connection.sendPacket(BlockPlaceC2SP(
                    position = blockPosition,
                    direction = raycastHit.hitDirection,
                    cursorPosition = raycastHit.hitPosition,
                    item = connection.player.inventory.getHotbarSlot(),
                    hand = Hands.MAIN_HAND,
                    insideBlock = false,  // ToDo
                ))
            }
        }

    }

    fun draw() {
        checkInteraction(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.BLOCK_INTERACT))
        checkBreaking(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.DESTROY_BLOCK))
    }
}
