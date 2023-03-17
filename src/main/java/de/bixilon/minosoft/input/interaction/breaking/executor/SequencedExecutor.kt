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

package de.bixilon.minosoft.input.interaction.breaking.executor

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler

class SequencedExecutor(breaking: BreakHandler) : BreakingExecutor(breaking) {


    override fun start(position: BlockPosition, state: BlockState): Int {
        super.start(position, state)
        return breaking.interactions.connection.sequence.getAndIncrement()
    }

    override fun finish(): Int {
        super.finish()
        return breaking.interactions.connection.sequence.getAndIncrement()
    }


    fun abort(position: BlockPosition, state: BlockState) {
        if (this.position != position || this.state != state) {
            return
        }
        abort = true
    }
}
