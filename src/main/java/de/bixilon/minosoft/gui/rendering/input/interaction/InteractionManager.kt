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
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP

class InteractionManager(
    val context: RenderContext,
) {
    private val connection = context.connection
    val hotbar = HotbarInteractionHandler(context, this)
    val pick = ItemPickInteractionHandler(context, this)
    val attack = AttackInteractionHandler(context, this)
    val `break` = BreakInteractionHandler(context)
    val use = InteractInteractionHandler(context, this)
    val drop = DropInteractionManager(context)
    val spectate = SpectateInteractionManager(context)

    private val swingArmRateLimiter = RateLimiter()

    // val equipProgress = floatArrayOf(0.0f, 0.0f) // for both hands; used in item rendering (animation up when equipping)

    fun init() {
        hotbar.init()
        pick.init()
        attack.init()
        `break`.init()
        use.init()
        drop.init()
        spectate.init()
    }

    fun draw(delta: Double) {
        hotbar.draw(delta)
        pick.draw(delta)
        attack.draw(delta)
        `break`.draw(delta)
        use.draw(delta)
        drop.draw(delta)
        spectate.draw(delta)

        swingArmRateLimiter.work()
    }

    fun swingHand(hand: Hands) {
        swingArmRateLimiter += { connection.sendPacket(SwingArmC2SP(hand)) }
    }

    fun isCoolingDown(item: Item): Boolean {
        connection.player.itemCooldown[item]?.let {
            if (it.ended) {
                connection.player.itemCooldown.remove(item)
            } else {
                return true
            }
        }
        return false
    }
}
