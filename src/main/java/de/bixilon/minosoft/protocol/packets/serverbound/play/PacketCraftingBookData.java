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

public class PacketCraftingBookData implements ServerboundPacket {
    final BookDataStatus action;

    final int recipeId;

    final boolean craftingBookOpen;
    final boolean craftingFilter;
    boolean blastingBookOpen;
    boolean blastingFilter;
    boolean smokingBookOpen;
    boolean smokingFilter;

    public PacketCraftingBookData(BookDataStatus action, int recipeId) {
        this.action = action;
        this.recipeId = recipeId;
        craftingBookOpen = false;
        craftingFilter = false;
    }

    public PacketCraftingBookData(BookDataStatus action, boolean craftingBookOpen, boolean craftingFilter) {
        this.action = action;
        this.recipeId = 0;
        this.craftingBookOpen = craftingBookOpen;
        this.craftingFilter = craftingFilter;
    }

    public PacketCraftingBookData(BookDataStatus action, boolean craftingBookOpen, boolean craftingFilter, boolean blastingBookOpen, boolean smokingFilter) {
        this.action = action;
        this.smokingFilter = smokingFilter;
        this.recipeId = 0;
        this.craftingBookOpen = craftingBookOpen;
        this.craftingFilter = craftingFilter;
        this.blastingBookOpen = blastingBookOpen;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_RECIPE_BOOK_DATA);
        if (buffer.getVersionId() < 333) {
            buffer.writeInt(action.ordinal());
        } else {
            buffer.writeVarInt(action.ordinal());
        }

        switch (action) {
            case DISPLAY_RECIPE -> buffer.writeVarInt(recipeId);
            case CRAFTING_BOOK_STATUS -> {
                buffer.writeBoolean(craftingBookOpen);
                buffer.writeBoolean(craftingFilter);
                if (buffer.getVersionId() >= 451) {
                    buffer.writeBoolean(blastingBookOpen);
                    buffer.writeBoolean(blastingFilter);
                    buffer.writeBoolean(smokingBookOpen);
                    buffer.writeBoolean(smokingFilter);
                }
            }
        }
        return buffer;
    }

    @Override
    public void log() {
        switch (action) {
            case DISPLAY_RECIPE -> Log.protocol(String.format("[OUT] Sending crafting book status (action=%s, recipeId=%d)", action, recipeId));
            case CRAFTING_BOOK_STATUS -> Log.protocol(String.format("[OUT] Sending crafting book status (action=%s, craftingBookOpen=%s, craftingFilter=%s)", action, craftingBookOpen, craftingFilter));
        }
    }

    public enum BookDataStatus {
        DISPLAY_RECIPE,
        CRAFTING_BOOK_STATUS;

        public static BookDataStatus byId(int id) {
            return values()[id];
        }
    }
}
