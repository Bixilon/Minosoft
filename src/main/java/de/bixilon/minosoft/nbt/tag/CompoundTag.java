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

import java.util.HashMap;
import java.util.Map;

public class CompoundTag implements Tag {
    final String name;
    final HashMap<String, Tag> data;

    public CompoundTag(String name, HashMap<String, Tag> data) {
        this.name = name;
        this.data = data;
    }

    public CompoundTag(boolean subTag, InByteBuffer buffer) {
        if (!subTag) {
            if (buffer.readByte() != TagTypes.COMPOUND.getId()) { // will be a Compound Tag
                // decompressed but still bad.... :(
                throw new IllegalArgumentException("Bad nbt");
            }
            // if in an array, there is no name
            this.name = buffer.readString(buffer.readShort()); // length
        } else {
            this.name = null;
        }
        this.data = new HashMap<>();
        while (true) {
            TagTypes tagType = TagTypes.getById(buffer.readByte());
            if (tagType == TagTypes.END) {
                //end tag
                break;
            }
            String tagName = buffer.readString(buffer.readShort()); // length
            switch (tagType) {
                case BYTE:
                    data.put(tagName, new ByteTag(buffer));
                    break;
                case SHORT:
                    data.put(tagName, new ShortTag(buffer));
                    break;
                case INT:
                    data.put(tagName, new IntTag(buffer));
                    break;
                case LONG:
                    data.put(tagName, new LongTag(buffer));
                    break;
                case FLOAT:
                    data.put(tagName, new FloatTag(buffer));
                    break;
                case DOUBLE:
                    data.put(tagName, new DoubleTag(buffer));
                    break;
                case BYTE_ARRAY:
                    data.put(tagName, new ByteArrayTag(buffer));
                    break;
                case STRING:
                    data.put(tagName, new StringTag(buffer));
                    break;
                case LIST:
                    data.put(tagName, new ListTag(buffer));
                    break;
                case COMPOUND:
                    data.put(tagName, new CompoundTag(true, buffer));
                    break;
            }
        }
    }

    public CompoundTag(InByteBuffer buffer) {
        this(false, buffer);
    }

    @Override
    public TagTypes getType() {
        return TagTypes.COMPOUND;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        buffer.writeByte((byte) TagTypes.COMPOUND.getId());
        buffer.writeShort((short) name.length());
        buffer.writeStringNoLength(name);
        // now with prefixed name, etc it is technically the same as a subtag
        writeBytesSubTag(buffer);
    }

    public void writeBytesSubTag(OutByteBuffer buffer) {
        for (Map.Entry<String, Tag> set : data.entrySet()) {
            buffer.writeByte((byte) set.getValue().getType().getId());
            buffer.writeShort((short) set.getKey().length());
            buffer.writeStringNoLength(set.getKey());

            // write data
            if (set.getValue() instanceof CompoundTag) {
                // that's a subtag! special rule
                CompoundTag compoundTag = (CompoundTag) set.getValue();
                compoundTag.writeBytesSubTag(buffer);
                continue;
            }
            set.getValue().writeBytes(buffer);

        }
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public ByteTag getByteTag(String key) {
        return (ByteTag) data.get(key);
    }

    public ShortTag getShortTag(String key) {
        return (ShortTag) data.get(key);
    }

    public IntTag getIntTag(String key) {
        return (IntTag) data.get(key);
    }

    public LongTag getLongTag(String key) {
        return (LongTag) data.get(key);
    }

    public FloatTag getFloatTag(String key) {
        return (FloatTag) data.get(key);
    }

    public DoubleTag getDoubleTag(String key) {
        return (DoubleTag) data.get(key);
    }

    public ByteArrayTag getByteArrayTag(String key) {
        return (ByteArrayTag) data.get(key);
    }

    public StringTag getStringTag(String key) {
        return (StringTag) data.get(key);
    }

    public ListTag getListTag(String key) {
        return (ListTag) data.get(key);
    }

    public CompoundTag getCompoundTag(String key) {
        return (CompoundTag) data.get(key);
    }

}
