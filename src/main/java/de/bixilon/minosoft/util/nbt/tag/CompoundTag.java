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

package de.bixilon.minosoft.util.nbt.tag;

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

import java.util.HashMap;
import java.util.Map;

public class CompoundTag implements NBTTag {
    final String name;
    final HashMap<String, NBTTag> data;

    public CompoundTag(String name, HashMap<String, NBTTag> data) {
        this.name = name;
        this.data = data;
    }

    public CompoundTag(boolean subTag, InByteBuffer buffer) {
        if (subTag) {
            this.name = null;
        } else {
            // if in an array, there is no name
            this.name = buffer.readString(buffer.readShort()); // length
        }
        this.data = new HashMap<>();
        while (true) {
            byte tag = buffer.readByte();
            TagTypes tagType = TagTypes.getById(tag);
            if (tagType == TagTypes.END) {
                //end tag
                break;
            }
            String tagName = buffer.readString(buffer.readShort()); // length
            data.put(tagName, buffer.readNBT(tagType));
        }
    }

    public CompoundTag(InByteBuffer buffer) {
        this(false, buffer);
    }

    public CompoundTag() {
        name = null;
        data = new HashMap<>();
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
        for (Map.Entry<String, NBTTag> set : data.entrySet()) {
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

    public void writeTag(String name, NBTTag tag) {
        data.put(name, tag);
    }

    // abstract functions

    public void writeBlockPosition(BlockPosition position) {
        data.put("x", new IntTag(position.getX()));
        data.put("y", new IntTag(position.getY()));
        data.put("z", new IntTag(position.getZ()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("{");

        for (Map.Entry<String, NBTTag> set : data.entrySet()) {
            builder.append(set.getKey());
            builder.append(": ");
            builder.append(set.getValue());
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length()); // delete last comma

        builder.append("}");
        return builder.toString();
    }
}
