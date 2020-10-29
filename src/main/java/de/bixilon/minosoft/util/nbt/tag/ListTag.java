/*
 * Minosoft
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

package de.bixilon.minosoft.util.nbt.tag;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;

public class ListTag extends NBTTag {
    final TagTypes type;
    final ArrayList<NBTTag> list;

    public ListTag(TagTypes type, ArrayList<NBTTag> list) {
        this.type = type;
        this.list = list;
    }

    public ListTag(TagTypes type, NBTTag[] list) {
        this.type = type;
        this.list = (ArrayList<NBTTag>) Arrays.asList(list);
    }

    public ListTag(InByteBuffer buffer) {
        this.type = TagTypes.getById(new ByteTag(buffer).getValue());
        int length = new IntTag(buffer).getValue();
        list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            switch (type) {
                case BYTE -> list.add(new ByteTag(buffer));
                case SHORT -> list.add(new ShortTag(buffer));
                case INT -> list.add(new IntTag(buffer));
                case LONG -> list.add(new LongTag(buffer));
                case FLOAT -> list.add(new FloatTag(buffer));
                case DOUBLE -> list.add(new DoubleTag(buffer));
                case BYTE_ARRAY -> list.add(new ByteArrayTag(buffer));
                case STRING -> list.add(new StringTag(buffer));
                case LIST -> list.add(new ListTag(buffer));
                case COMPOUND -> list.add(new CompoundTag(true, buffer));
            }
        }
    }

    @Override
    public TagTypes getType() {
        return TagTypes.LIST;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        new ByteTag((byte) type.ordinal()).writeBytes(buffer);

        new IntTag(list.size()).writeBytes(buffer);

        for (NBTTag NBTTag : list) {
            NBTTag.writeBytes(buffer);
        }
    }

    public <K extends NBTTag> ArrayList<K> getValue() {
        return (ArrayList<K>) list;
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
