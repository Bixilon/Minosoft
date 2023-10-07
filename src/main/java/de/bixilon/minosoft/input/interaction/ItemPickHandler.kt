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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

class ItemPickHandler(
    private val interactions: InteractionManager,
) {
    private val connection = interactions.connection
    val rateLimiter = RateLimiter()

    fun pickItem(copyNBT: Boolean) {
        if (connection.player.gamemode != Gamemodes.CREATIVE) {
            return
        }
        val target = connection.camera.target.target ?: return

        if (target.distance >= connection.player.reachDistance) {
            return
        }

        val stack: ItemStack?

        when (target) {
            is BlockTarget -> {
                val block = target.state.block

                stack = if (block is BlockWithItem<*>) ItemStackUtil.of(block.item, count = 1, connection = connection) else null

                if (copyNBT && stack != null) {
                    val blockEntity = connection.world.getBlockEntity(target.blockPosition)
                    blockEntity?.nbt?.toMutableMap()?.let { stack.updateNbt(it) }
                }
            }

            is EntityTarget -> {
                val entity = target.entity
                stack = entity.type.spawnEgg?.let { ItemStackUtil.of(it, connection = connection) } ?: entity.nullCast<LivingEntity>()?.equipment?.get(EquipmentSlots.MAIN_HAND)?.copy()
            }

            else -> stack = null
        }

        if (stack == null) {
            return
        }
        for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
            val slot = connection.player.items.inventory.getHotbarSlot(i) ?: continue
            if (!slot.matches(stack)) {
                continue
            }
            interactions.hotbar.selectSlot(i)
            return
        }
        var slot = connection.player.items.hotbar
        if (connection.player.items.inventory.getHotbarSlot(slot) != null) {
            for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
                val item = connection.player.items.inventory.getHotbarSlot(i)
                if (item == null) {
                    slot = i
                    break
                }
            }
        }
        interactions.hotbar.selectSlot(slot)
        val selectedSlot = connection.player.items.hotbar + PlayerInventory.HOTBAR_OFFSET

        rateLimiter += { connection.sendPacket(ItemStackCreateC2SP(selectedSlot, stack)) }
        connection.player.items.inventory[selectedSlot] = stack

        // ToDo: Use ItemPickC2SP
    }

    fun draw() {
        rateLimiter.work()
    }
}
