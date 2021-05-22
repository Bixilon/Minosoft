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
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.packets.c2s.play.ArmSwingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockBreakC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i

class LeftClickHandler(
    val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection

    private var breakPosition: Vec3i? = null
    private var breakBlockState: BlockState? = null
    private var breakPercentBroken: Float = -1.0f

    private var breakSelectedSlot: Int = -1
    private var breakItemInHand: ItemStack? = null

    private var breakSent = 0L
    private var creativeLastHoldBreakTime = 0L

    private fun clearDigging() {
        breakPosition = null
        breakBlockState = null
        breakPercentBroken = -1.0f

        breakSelectedSlot = -1
        breakItemInHand = null
    }

    private fun cancelDigging() {
        breakPosition?.let {
            connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.CANCELLED_DIGGING, breakPosition, Directions.UP)) // ToDo: Direction?
            clearDigging()
        }
    }

    private fun checkBreaking(isKeyDown: Boolean, deltaTime: Long) {
        val currentTime = System.currentTimeMillis()

        if (!isKeyDown) {
            creativeLastHoldBreakTime = 0L
            cancelDigging()
            return
        }

        if (!connection.player.entity.gamemode.canBreak) {
            cancelDigging()
            return
        }
        val raycastHit = renderWindow.inputHandler.camera.getTargetBlock()

        if (raycastHit == null) {
            cancelDigging()
            return
        }

        if (raycastHit.distance >= RenderConstants.MAX_BLOCK_OUTLINE_RAYCAST_DISTANCE) {
            cancelDigging()
            return
        }

        // check if we look at another block or our inventory changed
        if (breakPosition != raycastHit.blockPosition || breakBlockState != raycastHit.blockState || breakSelectedSlot != connection.player.selectedHotbarSlot || breakItemInHand !== connection.player.inventory.getHotbarSlot()) {
            cancelDigging()
        }


        fun startDigging() {
            if (breakPosition != null) {
                return
            }
            connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.START_DIGGING, raycastHit.blockPosition, raycastHit.hitDirection))

            breakPosition = raycastHit.blockPosition
            breakBlockState = raycastHit.blockState
            breakPercentBroken = 0.0f

            breakSelectedSlot = connection.player.selectedHotbarSlot
            breakItemInHand = connection.player.inventory.getHotbarSlot()
        }

        fun finishDigging() {
            connection.sendPacket(BlockBreakC2SP(BlockBreakC2SP.BreakType.FINISHED_DIGGING, raycastHit.blockPosition, raycastHit.hitDirection))
            clearDigging()
            connection.world.setBlockState(raycastHit.blockPosition, null)
        }

        if (currentTime - breakSent <= ProtocolDefinition.TICK_TIME) {
            return
        }
        breakSent = currentTime

        val canInstantBreak = connection.player.baseAbilities.canInstantBreak || connection.player.entity.gamemode == Gamemodes.CREATIVE

        if (canInstantBreak) {
            // creative
            if (currentTime - creativeLastHoldBreakTime <= ProtocolDefinition.TICK_TIME * 5) {
                return
            }
            connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))
            startDigging()
            finishDigging()
            creativeLastHoldBreakTime = currentTime
            return
        }

        startDigging()
        connection.sendPacket(ArmSwingC2SP(Hands.MAIN_HAND))


    }

    fun init() {
        renderWindow.inputHandler.registerCheckCallback(KeyBindingsNames.DESTROY_BLOCK)
    }

    fun draw(deltaTime: Long) {
        // ToDo: Entity attacking, arm swing animation when hitting air
        checkBreaking(renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.DESTROY_BLOCK), deltaTime)
    }
}
