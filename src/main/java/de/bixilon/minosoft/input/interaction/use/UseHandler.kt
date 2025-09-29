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

package de.bixilon.minosoft.input.interaction.use

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.input.interaction.InteractionManager
import de.bixilon.minosoft.input.interaction.KeyHandler
import de.bixilon.minosoft.protocol.packets.c2s.play.block.BlockInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A

class UseHandler(
    val interactions: InteractionManager,
) : KeyHandler() {
    val session = interactions.session
    private val short = ShortUseHandler(this)
    val long = LongUseHandler(this)

    private var autoInteractionDelay = 0

    private var previous = false


    override fun onPress() = tick(true)
    override fun onTick() = tick(true)
    override fun onRelease() = tick(false)

    private fun tick(interact: Boolean) {
        val previous = this.previous
        this.previous = interact

        if (!interact) {
            // not interacting anymore
            return long.reset()
        }

        val slot = session.player.items.hotbar

        if (long.isUsing) {
            // we do use an item (e.g. shield), continue use it
            autoInteractionDelay = AUTO_INTERACTION_COOLDOWN
            long.tick(slot)

            // still or again using, can not use short interaction
            if (long.isUsing) return
        }

        if (interactions.breaking.digging.status != null) {
            // we are breaking a block and can not start new interactions
            return
        }

        if (++autoInteractionDelay < AUTO_INTERACTION_COOLDOWN && previous) {
            // in auto interaction delay, because we are holding the key
            return
        }
        autoInteractionDelay = 0

        // TODO: check for riding status


        var target = interactions.camera.target.target
        if (target != null && target.distance >= session.player.reachDistance) {
            target = null
        }

        // check both hands if we can interact
        // if we can, stop further interactions
        for (hand in Hands.VALUES) {
            if (hand == Hands.OFF && !session.version.hasOffhand) {
                // only one hand available
                return
            }
            val stack = session.player.items.inventory[hand]

            if (target != null && short.tryUse(hand, target, stack)) {
                // try with target
                return
            }
            if (stack == null) {
                // no item in hand, can not further interact
                continue
            }
            if (!canInteractItem(stack)) {
                continue
            }

            // TODO: send both packets if item is cooling down

            val player = session.player
            player.physics().sender.sendPositionRotation()

            if (long.tryUse(hand, slot, stack)) return

            // try without target
            if (short.tryUse(hand, stack)) return
            sendItemUse(hand, stack)
        }
    }

    private fun canInteractItem(stack: ItemStack): Boolean {
        if (session.player.gamemode == Gamemodes.SPECTATOR) {
            return false
        }
        if (interactions.isCoolingDown(stack.item.item)) {
            return false
        }
        return true
    }


    fun sendItemUse(hand: Hands, stack: ItemStack) {
        if (session.version < V_15W31A) {
            session.connection.send(BlockInteractC2SP(null, null, null, stack, hand, false, 1))
        }
        session.connection.send(UseItemC2SP(hand, interactions.session.sequence.getAndIncrement()))
    }

    private companion object {
        const val AUTO_INTERACTION_COOLDOWN = 4
    }
}
