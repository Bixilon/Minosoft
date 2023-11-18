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

package de.bixilon.minosoft.input.interaction.breaking.creative

import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP

class CreativeBreaker(
    private val breaking: BreakHandler,
) {
    private val connection = breaking.interactions.connection

    fun breakBlock(target: BlockTarget?): Boolean {
        if (target == null) return false
        breaking.addCooldown()
        val sequence = breaking.executor.start(target.blockPosition, target.state)
        breaking.executor.finish()

        connection.network.send(PlayerActionC2SP(PlayerActionC2SP.Actions.START_DIGGING, target.blockPosition, target.direction, sequence))
        breaking.interactions.swingHand(Hands.MAIN)
        return true
    }
}
