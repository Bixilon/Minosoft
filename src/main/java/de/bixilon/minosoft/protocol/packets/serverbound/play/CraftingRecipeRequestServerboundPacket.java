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

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class CraftingRecipeRequestServerboundPacket implements PlayServerboundPacket {
    private final byte windowId;
    private final int recipeId;

    public CraftingRecipeRequestServerboundPacket(byte windowId, int recipeId) {
        this.windowId = windowId;
        this.recipeId = recipeId;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeVarInt(this.recipeId);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending entity action packet (windowId=%d, recipeId=%d)", this.windowId, this.recipeId));
    }
}
