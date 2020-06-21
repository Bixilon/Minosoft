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

package de.bixilon.minosoft.protocol.protocol;

import java.util.ArrayList;
import java.util.List;

public class OutPacketBuffer extends OutByteBuffer {
    private final int command;

    public OutPacketBuffer(int command) {
        super();
        this.command = command;
    }

    public int getCommand() {
        return command;
    }

    @Override
    public byte[] getOutBytes() {
        // ToDo: compression
        List<Byte> before = getBytes();
        List<Byte> after = new ArrayList<>();
        List<Byte> last = new ArrayList<>();
        writeVarInt(getCommand(), after); // second: command
        after.addAll(before); // rest ist raw data

        writeVarInt(after.size(), last); // first var int: length
        last.addAll(after); // rest ist raw data

        byte[] ret = new byte[last.size()];
        for (int i = 0; i < last.size(); i++) {
            ret[i] = last.get(i);
        }
        return ret;
    }

}
