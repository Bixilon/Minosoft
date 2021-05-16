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
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockPlaceC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class InteractionHandler(
    val renderWindow: RenderWindow,
) {
    private var lastInteraction = 0L
    private var lastInteractionSent = 0L

    fun init() {
        renderWindow.inputHandler.registerCheckCallback(KeyBindingsNames.BLOCK_INTERACT)
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

        when (usage) {
            BlockUsages.SUCCESS -> {
                if (currentTime - lastInteractionSent < ProtocolDefinition.TICK_TIME) {
                    return
                }
                renderWindow.connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))

                renderWindow.connection.sendPacket(BlockPlaceC2SP(
                    position = raycastHit.blockPosition,
                    direction = Directions.NORTH, // ToDo
                    cursorPosition = raycastHit.hitPosition,
                    item = null, // ToDo
                    hand = Hands.MAIN_HAND,
                    insideBlock = false,  // ToDo
                ))
                lastInteractionSent = currentTime
                lastInteraction = currentTime
            }
            BlockUsages.PASS -> {
                // use item or place block
            }
        }

    }

    fun draw() {
        checkInteraction(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.BLOCK_INTERACT))
    }
}
