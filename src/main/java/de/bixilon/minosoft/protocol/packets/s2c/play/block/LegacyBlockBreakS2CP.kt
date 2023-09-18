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
package de.bixilon.minosoft.protocol.packets.s2c.play.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.input.interaction.breaking.executor.LegacyExecutor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP.Actions
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class LegacyBlockBreakS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3i = buffer.readBlockPosition()
    val state: BlockState? = buffer.connection.registries.blockState.getOrNull(buffer.readVarInt())
    val action: Actions = Actions[buffer.readVarInt()]
    val successful: Boolean = buffer.readBoolean()

    override fun handle(connection: PlayConnection) {
        val executor = connection.camera.interactions.breaking.executor
        if (executor !is LegacyExecutor) return
        if (action != Actions.FINISHED_DIGGING && action != Actions.START_DIGGING && action != Actions.CANCELLED_DIGGING) return

        if (successful) {
            executor.acknowledge(position)
        } else {
            executor.revert(position)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Block break (position=$position, state=$state, action=$action, successful=$successful)" }
    }
}
