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

package de.bixilon.minosoft.util.nbt.tag;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

public class ByteArrayTag extends NBTTag {
    private final byte[] value;

    public ByteArrayTag(byte[] value) {
        this.value = value;
    }

    public ByteArrayTag(InByteBuffer buffer) {
        this.value = buffer.readByteArray(new IntTag(buffer).getValue());
    }

    @Override
    public NBTTagTypes getType() {
        return NBTTagTypes.BYTE_ARRAY;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        new IntTag(this.value.length).writeBytes(buffer);
        buffer.writeUnprefixedByteArray(this.value);
    }

    public byte[] getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < this.value.length; i++) {
            builder.append(this.value[i]);
            builder.append("b");
            if (i == this.value.length - 1) {
                break;
            }
            builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
