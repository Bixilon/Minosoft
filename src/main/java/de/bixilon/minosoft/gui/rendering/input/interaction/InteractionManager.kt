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

import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.RateLimiter
import de.bixilon.minosoft.protocol.packets.c2s.play.ArmSwingC2SP

class InteractionManager(
    val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    val hotbar = HotbarInteractionHandler(renderWindow)
    val pick = ItemPickInteractionHandler(renderWindow, this)
    val attack = AttackInteractionHandler(renderWindow)
    val `break` = BreakInteractionHandler(renderWindow)
    val use = InteractInteractionHandler(renderWindow, this)

    private val swingArmRateLimiter = RateLimiter()

    // val equipProgress = floatArrayOf(0.0f, 0.0f) // for both hands; used in item rendering (animation up when equipping)

    fun init() {
        hotbar.init()
        pick.init()
        `break`.init()
        use.init()
    }

    fun draw(delta: Double) {
        hotbar.draw(delta)
        pick.draw(delta)
        // attack.draw(delta)
        `break`.draw(delta)
        use.draw(delta)

        swingArmRateLimiter.work()
    }

    fun swingHand(hand: Hands) {
        swingArmRateLimiter += { connection.sendPacket(ArmSwingC2SP(hand)) }
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