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

package de.bixilon.minosoft.nbt.tag;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

public class LongArrayTag implements Tag {
    final long[] value;

    public LongArrayTag(long[] value) {
        this.value = value;
    }

    public LongArrayTag(InByteBuffer buffer) {
        this.value = buffer.readLongs(new IntTag(buffer).getValue());
    }

    @Override
    public TagTypes getType() {
        return TagTypes.LONG_ARRAY;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        new IntTag(value.length).writeBytes(buffer);
        buffer.writeLongs(value);
    }


    public long[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (long l : value) {
            builder.append(l);
            builder.append("L, ");
        }
        builder.delete(builder.length() - 1, builder.length()); // delete last comma
        builder.append("]");

        return builder.toString();
    }
}
