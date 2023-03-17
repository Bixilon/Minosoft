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

package de.bixilon.minosoft.input.interaction.breaking

import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.input.interaction.InteractionManager
import de.bixilon.minosoft.input.interaction.KeyHandler
import de.bixilon.minosoft.input.interaction.breaking.creative.CreativeBreaker
import de.bixilon.minosoft.input.interaction.breaking.executor.BreakingExecutor
import de.bixilon.minosoft.input.interaction.breaking.survival.SurvivalDigger

// TODO: this is actually 100% tick aligned
// I suggest that we start at any time, but stop (or restart) only after a delay of 50ms
class BreakHandler(
    val interactions: InteractionManager,
) : KeyHandler() {
    private var cooldown = -1
    private val connection = interactions.connection
    val executor = BreakingExecutor.create(this)

    val creative = CreativeBreaker(this)
    val digging = SurvivalDigger(this)


    private fun validateTarget(): BlockTarget? {
        if (!connection.player.gamemode.canBreak) return null
        if (!isPressed) return null

        val target = interactions.camera.target.target ?: return null


        if (target !is BlockTarget) return null
        if (!connection.world.isPositionChangeable(target.blockPosition)) return null
        if (target.distance >= connection.player.reachDistance) return null


        return target
    }

    override fun onPress() {
        if (cooldown-- >= 0) {
            interactions.swingHand(Hands.MAIN)
            return
        }
        tickBreaking()
    }

    override fun onRelease() {
        digging.tryCancel()
        cooldown = -1 // TODO: vanilla does not reset it when in survival, it still counts down
    }

    override fun onTick() {
        if (cooldown-- >= 0) {
            return
        }
        tickBreaking()
    }

    private fun tickBreaking() {
        val gamemode = connection.player.gamemode
        val target = validateTarget()

        if (gamemode == Gamemodes.CREATIVE) {
            digging.tryCancel()
            creative.breakBlock(target)
        } else {
            this.digging.dig(target)
        }
    }

    fun addCooldown() {
        cooldown = COOLDOWN
    }

    companion object {
        const val COOLDOWN = 5
    }
}
