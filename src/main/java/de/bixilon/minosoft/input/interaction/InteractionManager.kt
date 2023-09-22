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
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.camera.ConnectionCamera
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.input.interaction.InteractionUtil.canInteract
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler
import de.bixilon.minosoft.input.interaction.use.UseHandler
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class InteractionManager(val camera: ConnectionCamera) : Tickable {
    val connection = camera.connection
    val hotbar = HotbarHandler(this)
    val pick = ItemPickHandler(this)
    val attack = AttackHandler(this)
    val breaking = BreakHandler(this)
    val use = UseHandler(this)
    val drop = DropHandler(this)
    val spectate = SpectateHandler(this)

    private var lastTick = 0L

    private val swingArmRateLimiter = RateLimiter()


    fun init() {
        spectate.init()
    }

    @Deprecated("align with ticks and instant shots")
    fun draw() {
        tryTick()

        hotbar.draw()
        pick.draw()
        drop.draw()
        spectate.draw()

        swingArmRateLimiter.work()
    }

    private fun tryTick() {
        val time = millis()
        if (time - lastTick < ProtocolDefinition.TICK_TIME) {
            return
        }
        tick()
        lastTick = time
    }

    override fun tick() {
        attack.tick()
    }

    fun swingHand(hand: Hands) {
        swingArmRateLimiter += { connection.sendPacket(SwingArmC2SP(hand)) }
    }

    fun isCoolingDown(item: Item): Boolean {
        val cooldown = connection.player.items.cooldown[item] ?: return false
        if (cooldown.ended) {
            connection.player.items.cooldown -= item
            return false
        }
        return true
    }

    fun tryAttack(pressed: Boolean) {
        if (!pressed || use.long.isUsing || !connection.player.canInteract()) {
            return breaking.change(false)
        }
        when (val target = camera.target.target) {
            is EntityTarget -> {
                breaking.change(false)
                attack.tryAttack(target)
            }

            is BlockTarget -> breaking.change(true)
            else -> swingHand(Hands.MAIN)
        }
    }
}
