/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketRecipeBookState implements ServerboundPacket {
    final RecipeBooks book;
    final boolean bookOpen;
    final boolean filterActive;

    public PacketRecipeBookState(RecipeBooks book, boolean bookOpen, boolean filterActive) {
        this.book = book;
        this.bookOpen = bookOpen;
        this.filterActive = filterActive;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_SET_RECIPE_BOOK_STATE);
        buffer.writeVarInt(book.ordinal());
        buffer.writeBoolean(bookOpen);
        buffer.writeBoolean(filterActive);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending recipe book state (book=%s, bookOpen=%s, filterActive=%s)", book, bookOpen, filterActive));
    }

    public enum RecipeBooks {
        CRAFTING,
        FURNACE,
        BLAST_FURNACE,
        SMOKER;

        public static RecipeBooks byId(int id) {
            return values()[id];
        }
    }
}
