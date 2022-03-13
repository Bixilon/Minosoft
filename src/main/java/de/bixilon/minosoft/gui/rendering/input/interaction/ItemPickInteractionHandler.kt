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

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ItemPickInteractionHandler(
    val renderWindow: RenderWindow,
    val interactionManager: InteractionManager,
) {
    private val connection = renderWindow.connection
    val rateLimiter = RateLimiter()


    fun init() {
        renderWindow.inputHandler.registerKeyCallback("minosoft:pick_item".toResourceLocation(), KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.MOUSE_BUTTON_MIDDLE),
            ),
        )) {
            pickItem(true) // ToDo: Combination for not copying nbt
        }
    }

    fun pickItem(copyNBT: Boolean) {
        if (!connection.player.baseAbilities.creative) {
            return
        }
        val target = renderWindow.camera.targetHandler.target ?: return

        if (target.distance > connection.player.reachDistance) {
            return
        }

        val stack: ItemStack?

        when (target) {
            is BlockTarget -> {
                stack = ItemStackUtil.of(target.blockState.block.item, count = 1, connection = connection)

                if (copyNBT) {
                    val blockEntity = connection.world.getBlockEntity(target.blockPosition)
                    blockEntity?.nbt?.toMutableMap()?.let { stack.updateNbt(it) }
                }
            }
            is EntityTarget -> {
                val entity = target.entity
                stack = entity.type.spawnEgg?.let { ItemStackUtil.of(it, connection = connection) } ?: let {
                    entity.equipment[InventorySlots.EquipmentSlots.MAIN_HAND]?.copy()
                }
            }
            else -> {
                stack = null
            }
        }

        if (stack == null) {
            return
        }
        for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
            val slot = connection.player.inventory.getHotbarSlot(i) ?: continue
            if (slot != stack) {
                continue
            }
            interactionManager.hotbar.selectSlot(i)
            return
        }
        var slot = connection.player.selectedHotbarSlot
        if (connection.player.inventory.getHotbarSlot(slot) != null) {
            for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
                val item = connection.player.inventory.getHotbarSlot(i)
                if (item == null) {
                    slot = i
                    break
                }
            }
        }
        interactionManager.hotbar.selectSlot(slot)
        val selectedSlot = connection.player.selectedHotbarSlot + PlayerInventory.HOTBAR_OFFSET

        rateLimiter += { connection.sendPacket(ItemStackCreateC2SP(selectedSlot, stack)) }
        connection.player.inventory[selectedSlot] = stack

        // ToDo: Use ItemPickC2SP
    }

    fun draw() {
        rateLimiter.work()
    }
}
