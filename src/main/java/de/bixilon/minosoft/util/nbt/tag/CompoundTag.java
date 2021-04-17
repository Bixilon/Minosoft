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
import glm_.vec3.Vec3i;
import org.checkerframework.common.value.qual.IntRange;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CompoundTag extends NBTTag {
    private final String name;
    private final HashMap<String, NBTTag> data;

    public CompoundTag(String name, HashMap<String, NBTTag> data) {
        this.name = name;
        this.data = data;
    }

    public CompoundTag(InByteBuffer buffer) {
        this(false, buffer);
    }

    public CompoundTag(boolean subTag, InByteBuffer buffer) {
        if (subTag) {
            this.name = null;
        } else {
            // if in an array, there is no name
            this.name = buffer.readString(buffer.readUnsignedShort()); // length
        }
        this.data = new HashMap<>();
        while (true) {
            byte tag = buffer.readByte();
            NBTTagTypes tagType = NBTTagTypes.Companion.getVALUES()[tag];
            if (tagType == NBTTagTypes.END) {
                // end tag
                break;
            }
            String tagName = buffer.readString(buffer.readUnsignedShort()); // length
            this.data.put(tagName, buffer.readNBT(tagType));
        }
    }

    public CompoundTag() {
        this.name = null;
        this.data = new HashMap<>();
    }

    @Override
    public NBTTagTypes getType() {
        return NBTTagTypes.COMPOUND;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        buffer.writeByte((byte) NBTTagTypes.COMPOUND.ordinal());
        buffer.writeShort((short) this.name.length());
        buffer.writeUnprefixedString(this.name);
        // now with prefixed name, etc it is technically the same as a subtag
        writeBytesSubTag(buffer);
    }

    public void writeBytesSubTag(OutByteBuffer buffer) {
        for (Map.Entry<String, NBTTag> set : this.data.entrySet()) {
            buffer.writeByte((byte) set.getValue().getType().ordinal());
            buffer.writeShort((short) set.getKey().length());
            buffer.writeUnprefixedString(set.getKey());

            // write data
            if (set.getValue() instanceof CompoundTag compoundTag) {
                // that's a subtag! special rule
                compoundTag.writeBytesSubTag(buffer);
                continue;
            }
            set.getValue().writeBytes(buffer);
        }
    }

    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }

    public ByteTag getByteTag(String key) {
        return (ByteTag) this.data.get(key);
    }

    public ShortTag getShortTag(String key) {
        return (ShortTag) this.data.get(key);
    }

    public IntTag getIntTag(String key) {
        return (IntTag) this.data.get(key);
    }

    public LongTag getLongTag(String key) {
        return (LongTag) this.data.get(key);
    }

    public FloatTag getFloatTag(String key) {
        return (FloatTag) this.data.get(key);
    }

    public DoubleTag getDoubleTag(String key) {
        return (DoubleTag) this.data.get(key);
    }

    public ByteArrayTag getByteArrayTag(String key) {
        return (ByteArrayTag) this.data.get(key);
    }

    public StringTag getStringTag(String key) {
        return (StringTag) this.data.get(key);
    }

    public ListTag getListTag(String key) {
        return (ListTag) this.data.get(key);
    }

    public CompoundTag getCompoundTag(String key) {
        return (CompoundTag) this.data.get(key);
    }

    public NumberTag getNumberTag(String key) {
        return (NumberTag) this.data.get(key);
    }

    public NBTTag getTag(String key) {
        return this.data.get(key);
    }

    @Nullable
    public Boolean getBoolean(String key) {
        NumberTag tag = getNumberTag(key);
        if (tag == null) {
            return null;
        }
        return tag.getAsByte() == 0x01;
    }

    public void writeTag(String name, NBTTag tag) {
        if (this.isFinal) {
            throw new IllegalArgumentException("This tag is marked as final!");
        }
        this.data.put(name, tag);
    }

    @IntRange(from = 0)
    public int size() {
        return this.data.size();
    }

    // abstract functions

    public void writeBlockPosition(Vec3i position) {
        if (this.isFinal) {
            throw new IllegalArgumentException("This tag is marked as final!");
        }
        this.data.put("x", new IntTag(position.getX()));
        this.data.put("y", new IntTag(position.getY()));
        this.data.put("z", new IntTag(position.getZ()));
    }

    public CompoundTag removeKey(String key) {
        this.data.remove(key);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public HashMap<String, NBTTag> getData() {
        return this.data;
    }

    @Nullable
    public NBTTag getAndRemoveTag(String[] names) {
        for (var name : names) {
            var tag = getData().get(name);
            if (tag == null) {
                continue;
            }
            getData().remove(name);
            return tag;
        }
        return null;
    }

    @Nullable
    public NBTTag getAndRemoveTag(String name) {
        var tag = getData().get(name);
        if (tag == null) {
            return null;
        }
        getData().remove(name);
        return tag;
    }

    public CompoundTag clone() {
        return new CompoundTag(this.name, (HashMap<String, NBTTag>) this.data.clone());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.name != null) {
            builder.append(this.name);
        }
        builder.append('{');
        AtomicInteger i = new AtomicInteger();
        this.data.forEach((key, value) -> {
            builder.append(key);
            builder.append(": ");
            builder.append(value);
            if (i.get() == this.data.size() - 1) {
                return;
            }
            builder.append(", ");
            i.getAndIncrement();
        });
        builder.append("}");
        return builder.toString();
    }
}
