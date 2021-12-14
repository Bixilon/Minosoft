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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.other.containers.PlayerInventory
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.protocol.RateLimiter
import de.bixilon.minosoft.protocol.packets.c2s.play.ItemStackCreateC2SP
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

        val itemStack: ItemStack?

        when (target) {
            is BlockTarget -> {
                itemStack = ItemStack(target.blockState.block.item!!, connection, 1)

                if (copyNBT) {
                    val blockEntity = connection.world.getBlockEntity(target.blockPosition)
                    blockEntity?.nbt?.let { itemStack.nbt.putAll(it) }
                }
            }
            is EntityTarget -> {
                val entity = target.entity
                itemStack = entity.entityType.spawnEgg?.let { ItemStack(it, connection) } ?: let {
                    entity.equipment[InventorySlots.EquipmentSlots.MAIN_HAND]?.copy()
                }
            }
            else -> {
                itemStack = null
            }
        }

        if (itemStack == null) {
            return
        }
        for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
            val slot = connection.player.inventory.getHotbarSlot(i) ?: continue
            if (slot != itemStack) {
                continue
            }
            interactionManager.hotbar.selectSlot(i)
            return
        }
        val selectedSlot = connection.player.selectedHotbarSlot + PlayerInventory.HOTBAR_OFFSET

        rateLimiter += { connection.sendPacket(ItemStackCreateC2SP(selectedSlot, itemStack)) }
        connection.player.inventory[selectedSlot] = itemStack
    }

    fun draw(delta: Double) {
        rateLimiter.work()
    }
}
