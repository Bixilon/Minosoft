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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketWindowProperty extends ClientboundPacket {
    private final byte windowId;
    private final short property;
    private final short value;

    public PacketWindowProperty(InByteBuffer buffer) {
        this.windowId = buffer.readByte();
        this.property = buffer.readShort();
        this.value = buffer.readShort();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received window property (windowId=%d, property=%d, value=%d)", this.windowId, this.property, this.value));
    }

    public byte getWindowId() {
        return this.windowId;
    }

    public short getProperty() {
        return this.property;
    }

    public short getValue() {
        return this.value;
    }
}
