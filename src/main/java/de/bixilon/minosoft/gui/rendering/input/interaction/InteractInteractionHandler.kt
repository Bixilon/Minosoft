/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.items.UsableItem
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class InteractInteractionHandler(
    val renderWindow: RenderWindow,
    val interactionManager: InteractionManager,
) {
    val connection = renderWindow.connection
    private var lastUse = -1L

    private var interactingItem: ItemStack? = null
    private var interactingSlot: Int = -1
    private var interactingTicksLeft = 0

    private var previousDown = false
    private var autoInteractionDelay = 0


    fun init() {
        renderWindow.inputHandler.registerCheckCallback(USE_ITEM_KEYBINDING to KeyBinding(
            mapOf(
                KeyAction.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_RIGHT),
            ),
        ))
    }

    fun stopUsingItem() {
        if (!connection.player.isUsingItem) {
            return
        }
        connection.player.apply {
            isUsingItem = false
            activeHand = null
        }
        connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        interactingItem = null
        interactingSlot = -1
        interactingTicksLeft = 0
    }

    fun interactBlock(target: BlockTarget, stack: ItemStack?, hand: Hands): InteractionResults {
        if (target.distance >= connection.player.reachDistance) {
            return InteractionResults.PASS
        }
        // if out of world (border): return CONSUME

        try {
            if (connection.player.gamemode == Gamemodes.SPECTATOR) {
                return InteractionResults.SUCCESS
            }

            val result = target.blockState.block.onUse(connection, target, hand, stack)
            if (result == InteractionResults.SUCCESS) {
                return InteractionResults.SUCCESS
            }

            if (stack == null) {
                return InteractionResults.PASS
            }
            if (interactionManager.isCoolingDown(stack.item.item)) {
                return InteractionResults.PASS // ToDo: Check
            }

            return stack.item.item.interactBlock(connection, target, hand, stack)
        } finally {
            connection.sendPacket(BlockInteractC2SP(
                position = target.blockPosition,
                direction = target.direction,
                cursorPosition = Vec3(target.hitPosition),
                item = stack,
                hand = hand,
                insideBlock = false, // ToDo: insideBlock
            ))
        }
    }

    fun interactEntityAt(target: EntityTarget, hand: Hands): InteractionResults {
        // used in armor stands
        val player = connection.player
        connection.sendPacket(EntityInteractPositionC2SP(connection, target.entity, Vec3(target.position), hand, player.isSneaking))

        if (player.gamemode == Gamemodes.SPECTATOR) {
            return InteractionResults.PASS
        }
        // ToDo:  return hit.entity.interactAt(hit.position, hand)
        return InteractionResults.PASS
    }

    fun interactEntity(target: EntityTarget, hand: Hands): InteractionResults {
        val player = connection.player
        try {

            if (player.gamemode == Gamemodes.SPECTATOR) {
                return InteractionResults.PASS
            }

            // ToDo: return hit.entity.interact(hand) (e.g. equipping saddle)
            return InteractionResults.PASS
        } finally {
            connection.sendPacket(EntityEmptyInteractC2SP(connection, target.entity, hand, player.isSneaking))
        }
    }

    fun interactItem(item: ItemStack, hand: Hands): InteractionResults {
        if (connection.player.gamemode == Gamemodes.SPECTATOR) {
            return InteractionResults.SUCCESS
        }
        val player = connection.player
        connection.sendPacket(PositionRotationC2SP(player.position, player.rotation, player.onGround))

        try {

            if (interactionManager.isCoolingDown(item.item.item)) {
                return InteractionResults.PASS
            }


            return item.item.item.interactItem(connection, hand, item)
        } finally {
            // ToDo: Before 1.9
            connection.sendPacket(UseItemC2SP(hand))
        }
    }

    fun useItem() {
        if (interactionManager.`break`.breakingBlock) {
            return
        }

        // if riding: return

        val selectedSlot = connection.player.selectedHotbarSlot
        val target = renderWindow.camera.targetHandler.target

        for (hand in Hands.VALUES) {
            val item = connection.player.inventory[hand]
            when (target) {
                is EntityTarget -> {
                    var result = interactEntityAt(target, hand)

                    if (result == InteractionResults.PASS) {
                        result = interactEntity(target, hand)
                    }

                    if (result == InteractionResults.SUCCESS) {
                        interactionManager.swingHand(hand)
                        return
                    }
                    if (result == InteractionResults.CONSUME) {
                        return
                    }
                }
                is BlockTarget -> {
                    val result = interactBlock(target, item, hand)
                    if (result == InteractionResults.SUCCESS) {
                        interactionManager.swingHand(hand)
                        // ToDo: Reset equip progress
                        return
                    }
                    if (result == InteractionResults.CONSUME) {
                        return
                    }
                }
            }

            if (item != interactingItem || interactingSlot != selectedSlot) {
                interactingItem = item
                interactingSlot = selectedSlot
                val itemType = item?.item?.item
                interactingTicksLeft = if (itemType is UsableItem) {
                    itemType.maxUseTime
                } else {
                    0
                }
            }

            if (item == null) {
                continue
            }

            val result = interactItem(item, hand)

            if (result == InteractionResults.SUCCESS) {
                interactionManager.swingHand(hand)
                // ToDo: Reset equip progress
                return
            }
            if (result == InteractionResults.CONSUME) {
                return
            }
        }
    }

    fun draw(delta: Double) {
        val time = TimeUtil.millis
        if (time - lastUse < ProtocolDefinition.TICK_TIME) {
            return
        }
        lastUse = time
        val keyDown = renderWindow.inputHandler.isKeyBindingDown(USE_ITEM_KEYBINDING)
        if (keyDown) {
            autoInteractionDelay++

            val interactingItem = interactingItem
            val item = interactingItem?.item?.item
            if (item is UsableItem && connection.player.isUsingItem) {
                interactingTicksLeft--
                if (interactingTicksLeft < 0) {
                    item.finishUsing(connection, interactingItem)
                    stopUsingItem()
                }
            }
        } else {
            interactingTicksLeft = 0
            autoInteractionDelay = 0
            stopUsingItem()
        }
        if (keyDown && (!previousDown || (autoInteractionDelay >= 5 && interactingTicksLeft <= 0))) {
            useItem()
            autoInteractionDelay = 0
        }
        previousDown = keyDown
    }

    companion object {
        private val USE_ITEM_KEYBINDING = "minosoft:item_use".toResourceLocation()
    }
}
