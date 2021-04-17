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

import java.util.ArrayList;
import java.util.Arrays;

public class ListTag extends NBTTag {
    private final ArrayList<NBTTag> list;
    private NBTTagTypes type;

    public ListTag(NBTTagTypes type, ArrayList<NBTTag> list) {
        this.type = type;
        this.list = list;
    }

    public ListTag(NBTTagTypes type, NBTTag... list) {
        this.type = type;
        this.list = new ArrayList<>(Arrays.asList(list));
    }


    public ListTag() {
        this.type = null;
        this.list = new ArrayList<>();
    }

    public ListTag(InByteBuffer buffer) {
        this.type = NBTTagTypes.Companion.getVALUES()[new ByteTag(buffer).getValue()];
        int length = new IntTag(buffer).getValue();
        this.list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            this.list.add(switch (this.type) {
                case BYTE -> new ByteTag(buffer);
                case SHORT -> new ShortTag(buffer);
                case INT -> new IntTag(buffer);
                case LONG -> new LongTag(buffer);
                case FLOAT -> new FloatTag(buffer);
                case DOUBLE -> new DoubleTag(buffer);
                case BYTE_ARRAY -> new ByteArrayTag(buffer);
                case STRING -> new StringTag(buffer);
                case LIST -> new ListTag(buffer);
                case COMPOUND -> new CompoundTag(true, buffer);
                default -> throw new IllegalStateException("Unexpected value: " + this.type);
            });
        }
    }

    @Override
    public NBTTagTypes getType() {
        return NBTTagTypes.LIST;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        if (this.type == null) {
            this.type = NBTTagTypes.BYTE; // idk, default value?
        }
        new ByteTag((byte) this.type.ordinal()).writeBytes(buffer);

        new IntTag(this.list.size()).writeBytes(buffer);

        for (NBTTag tag : this.list) {
            tag.writeBytes(buffer);
        }
    }

    public ListTag addTag(NBTTag tag) {
        if (this.type == null) {
            this.type = tag.getType();
        } else {
            if (this.type != tag.getType()) {
                throw new IllegalArgumentException("Can not mix types!");
            }
        }
        this.list.add(tag);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <K extends NBTTag> ArrayList<K> getValue() {
        return (ArrayList<K>) this.list;
    }

    @Override
    public String toString() {
        return this.list.toString();
    }
}
