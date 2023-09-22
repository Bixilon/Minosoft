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

package de.bixilon.minosoft.protocol.network.connection.play.util

import de.bixilon.kutil.time.TimeUtil.nanos
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.NextChunkBatchC2SP

class ChunkReceiver(
    private val connection: PlayConnection,
) {
    private var count = 0
    private var start = -1L
    private var expected = Int.MAX_VALUE

    @Synchronized
    fun onBatchStart() {
        reset()
        start = nanos()
    }

    @Synchronized
    fun onBatchDone(size: Int) {
        expected = size
        checkExpected()
    }

    @Synchronized
    fun onChunk() {
        count++
        checkExpected()
    }

    fun reset() {
        count = 0
        start = -1L
        expected = Int.MAX_VALUE
    }

    private fun checkExpected() {
        if (count < expected) return
        val end = nanos()
        val delta = end - start

        connection.sendPacket(NextChunkBatchC2SP(100.0f)) // TODO: calculate size
        reset()
    }
}
