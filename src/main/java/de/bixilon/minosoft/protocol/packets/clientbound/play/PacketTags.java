/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.Tag;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketTags implements ClientboundPacket {
    Tag[] blockTags;
    Tag[] itemTags;
    Tag[] fluidTags;
    Tag[] entityTags;


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_13_2:
                blockTags = readTags(buffer);
                itemTags = readTags(buffer);
                fluidTags = readTags(buffer);
                return true;
            case VERSION_1_14_4:
                blockTags = readTags(buffer);
                itemTags = readTags(buffer);
                fluidTags = readTags(buffer);
                entityTags = readTags(buffer);
                return true;
        }
        return false;
    }

    private Tag[] readTags(InByteBuffer buffer) {
        Tag[] ret = new Tag[buffer.readVarInt()];
        switch (buffer.getVersion()) {
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = new Tag(buffer.readString(), buffer.readVarIntArray(buffer.readVarInt()));
                }
                break;
        }
        return ret;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received tags (blockLength=%d, itemLength=%d, fluidLength=%d, entityLength=%d)", blockTags.length, itemTags.length, fluidTags.length, ((entityTags == null) ? 0 : entityTags.length)));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
