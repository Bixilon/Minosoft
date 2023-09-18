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

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP

class DropHandler(
    private val interactions: InteractionManager,
) {
    private val connection = interactions.connection
    private val rateLimiter = RateLimiter()

    fun dropItem(stack: Boolean) {
        val type = if (stack) {
            connection.player.items.inventory.getHotbarSlot()?.item?.count = 0
            PlayerActionC2SP.Actions.DROP_ITEM_STACK
        } else {
            connection.player.items.inventory.getHotbarSlot()?.item?.decreaseCount()
            PlayerActionC2SP.Actions.DROP_ITEM
        }
        rateLimiter += { connection.sendPacket(PlayerActionC2SP(type)) }
    }

    fun draw() {
        rateLimiter.work()
    }
}
