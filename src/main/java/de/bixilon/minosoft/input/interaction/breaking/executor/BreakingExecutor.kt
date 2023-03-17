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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.handler.entity.BlockBreakHandler
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.misc.event.world.handler.BlockDestroyedHandler
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.interaction.breaking.BreakHandler
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W34A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W11A

abstract class BreakingExecutor(protected val breaking: BreakHandler) {
    protected var position: BlockPosition? = null
    protected var state: BlockState? = null
    protected var abort = false


    open fun cancel() {
        reset()
    }

    open fun start(position: BlockPosition, state: BlockState): Int {
        this.position = position
        this.state = state
        abort = false
        return 0
    }

    open fun finish(): Int {
        val state = this.state ?: Broken()
        val position = this.position ?: Broken()
        reset()
        if (!abort) {
            execute(position, state)
        }
        return 0
    }


    protected fun reset() {
        this.position = null
        this.state = null
        this.abort = false
    }


    protected fun execute(position: BlockPosition, state: BlockState) {
        DefaultThreadPool += {
            val connection = breaking.interactions.connection
            connection.world[position] = null
            if (state.block is BlockBreakHandler) {
                state.block.onBreak(connection, position, state, null)  // TODO: block entity
            }
            BlockDestroyedHandler.handleDestroy(connection, position, state)
        }
    }

    companion object {

        fun create(breaking: BreakHandler): BreakingExecutor {
            val version = breaking.interactions.connection.version
            return when {
                version < V_19W34A -> DirectExecutor(breaking)
                version < V_22W11A -> LegacyExecutor(breaking)
                else -> SequencedExecutor(breaking)
            }
        }
    }
}
