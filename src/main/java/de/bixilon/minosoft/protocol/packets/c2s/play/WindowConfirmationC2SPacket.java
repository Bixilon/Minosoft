/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.c2s.play;

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class WindowConfirmationC2SPacket implements PlayC2SPacket {
    private final byte windowId;
    private final short actionNumber;
    private final boolean accepted;

    public WindowConfirmationC2SPacket(byte windowId, short actionNumber, boolean accepted) {
        this.windowId = windowId;
        this.actionNumber = actionNumber;
        this.accepted = accepted;
    }

    @Override
    public void write(PlayOutByteBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.actionNumber);
        buffer.writeBoolean(this.accepted);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending confirm transaction packet (windowId=%d, actionNumber=%d, accepted=%s)", this.windowId, this.actionNumber, this.accepted));
    }
}
