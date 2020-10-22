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

import de.bixilon.minosoft.data.Tag;
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
        blockTags = readTags(buffer);
        itemTags = readTags(buffer);
        fluidTags = readTags(buffer); // ToDo: when was this added? Was not available in 18w01
        if (buffer.getVersionId() >= 440) {
            entityTags = readTags(buffer);
        }
        return true;
    }

    private Tag[] readTags(InByteBuffer buffer) {
        Tag[] ret = new Tag[buffer.readVarInt()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Tag(buffer.readString(), buffer.readVarIntArray(buffer.readVarInt()));
        }
        return ret;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received tags (blockLength=%d, itemLength=%d, fluidLength=%d, entityLength=%d)", blockTags.length, itemTags.length, fluidTags.length, ((entityTags == null) ? 0 : entityTags.length)));
    }
}
