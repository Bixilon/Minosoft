/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
    private val session = interactions.session
    val rateLimiter = RateLimiter()

    private fun getStack(copyNBT: Boolean): ItemStack? {
        val target = session.camera.target.target ?: return null

        if (target.distance >= session.player.reachDistance) {
            return null
        }

        when (target) {
            is BlockTarget -> {
                val block = target.state.block

                val stack = if (block is BlockWithItem<*>) ItemStackUtil.of(block.item, count = 1) else null

                if (copyNBT && stack != null) {
                    val blockEntity = session.world.getBlockEntity(target.blockPosition)
                    blockEntity?.nbt?.toMutableMap()?.let { stack.updateNbt(it) }
                }
                return stack
            }

            is EntityTarget -> {
                val entity = target.entity
                return entity.type.spawnEgg?.let { ItemStackUtil.of(it) } ?: entity.nullCast<LivingEntity>()?.equipment?.get(EquipmentSlots.MAIN_HAND)?.copy()
            }

            else -> return null
        }
    }

    fun pickItem(copyNBT: Boolean) {
        if (session.player.gamemode != Gamemodes.CREATIVE) {
            return
        }
        val stack = getStack(copyNBT) ?: return
        for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
            val slot = session.player.items.inventory.getHotbarSlot(i) ?: continue
            if (!slot.matches(stack)) {
                continue
            }
            interactions.hotbar.selectSlot(i)
            return
        }
        var slot = session.player.items.hotbar
        if (session.player.items.inventory.getHotbarSlot(slot) != null) {
            for (i in 0 until PlayerInventory.HOTBAR_SLOTS) {
                val item = session.player.items.inventory.getHotbarSlot(i)
                if (item == null) {
                    slot = i
                    break
                }
            }
        }
        interactions.hotbar.selectSlot(slot)
        val selectedSlot = session.player.items.hotbar + PlayerInventory.HOTBAR_OFFSET

        rateLimiter += { session.connection.send(ItemStackCreateC2SP(selectedSlot, stack)) }
        session.player.items.inventory[selectedSlot] = stack

        // ToDo: Use ItemPickC2SP
    }

    fun draw() {
        rateLimiter.work()
    }
}
