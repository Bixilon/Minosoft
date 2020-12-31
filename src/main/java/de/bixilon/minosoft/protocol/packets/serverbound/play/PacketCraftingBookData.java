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

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W50A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_PRE6;

public class PacketCraftingBookData implements ServerboundPacket {
    private final BookDataStatus action;
    private final int recipeId;
    private final boolean craftingBookOpen;
    private final boolean craftingFilter;
    boolean blastingBookOpen;
    boolean blastingFilter;
    boolean smokingBookOpen;
    boolean smokingFilter;

    public PacketCraftingBookData(BookDataStatus action, int recipeId) {
        this.action = action;
        this.recipeId = recipeId;
        this.craftingBookOpen = false;
        this.craftingFilter = false;
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
        if (buffer.getVersionId() < V_1_12_PRE6) {
            buffer.writeInt(this.action.ordinal());
        } else {
            buffer.writeVarInt(this.action.ordinal());
        }

        switch (this.action) {
            case DISPLAY_RECIPE -> buffer.writeVarInt(this.recipeId);
            case CRAFTING_BOOK_STATUS -> {
                buffer.writeBoolean(this.craftingBookOpen);
                buffer.writeBoolean(this.craftingFilter);
                if (buffer.getVersionId() >= V_18W50A) {
                    buffer.writeBoolean(this.blastingBookOpen);
                    buffer.writeBoolean(this.blastingFilter);
                    buffer.writeBoolean(this.smokingBookOpen);
                    buffer.writeBoolean(this.smokingFilter);
                }
            }
        }
        return buffer;
    }

    @Override
    public void log() {
        switch (this.action) {
            case DISPLAY_RECIPE -> Log.protocol(String.format("[OUT] Sending crafting book status (action=%s, recipeId=%d)", this.action, this.recipeId));
            case CRAFTING_BOOK_STATUS -> Log.protocol(String.format("[OUT] Sending crafting book status (action=%s, craftingBookOpen=%s, craftingFilter=%s)", this.action, this.craftingBookOpen, this.craftingFilter));
        }
    }

    public enum BookDataStatus {
        DISPLAY_RECIPE,
        CRAFTING_BOOK_STATUS;

        private static final BookDataStatus[] BOOK_DATA_STATUSES = values();

        public static BookDataStatus byId(int id) {
            return BOOK_DATA_STATUSES[id];
        }
    }
}
