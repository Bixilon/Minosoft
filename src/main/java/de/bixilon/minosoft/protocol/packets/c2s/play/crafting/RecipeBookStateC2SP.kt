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
package de.bixilon.minosoft.protocol.packets.c2s.play.crafting

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RecipeBookStateC2SP(
    val book: RecipeBooks,
    val bookOpen: Boolean,
    val filterActive: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeVarInt(book.ordinal)
        buffer.writeBoolean(bookOpen)
        buffer.writeBoolean(filterActive)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Recipe book state (book=$book, bookOpen=$bookOpen, filterActive=$filterActive)" }
    }

    enum class RecipeBooks {
        CRAFTING,
        FURNACE,
        BLAST_FURNACE,
        SMOKER,
        ;
    }
}
