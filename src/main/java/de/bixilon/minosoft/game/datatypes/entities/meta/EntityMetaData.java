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
package de.bixilon.minosoft.game.datatypes.entities.meta;

import de.bixilon.minosoft.game.datatypes.*;
import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;


public class EntityMetaData {

    final HashMap<Integer, MetaDataSet> sets;
    final ProtocolVersion version;

    /*
    1.7.10: https://wiki.vg/index.php?title=Entity_metadata&oldid=5991
    1.8: https://wiki.vg/index.php?title=Entity_metadata&oldid=6611
    1.9.4: https://wiki.vg/index.php?title=Entity_metadata&oldid=7955
    1.10: https://wiki.vg/index.php?title=Entity_metadata&oldid=8241
    1.13: https://wiki.vg/index.php?title=Entity_metadata&oldid=14800
     */
    public EntityMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        this.sets = sets;
        this.version = version;
    }

    public static Object getData(EntityMetaData.Types type, InByteBuffer buffer) {
        Object data = null;

        switch (type) {
            case BYTE:
                data = buffer.readByte();
                break;
            case VAR_INT:
                data = buffer.readVarInt();
                break;
            case SHORT:
                data = buffer.readShort();
                break;
            case INT:
                data = buffer.readInt();
                break;
            case FLOAT:
                data = buffer.readFloat();
                break;
            case STRING:
                data = buffer.readString();
                break;
            case CHAT:
                data = buffer.readTextComponent();
                break;
            case BOOLEAN:
                data = buffer.readBoolean();
                break;
            case VECTOR:
                data = new Vector(buffer.readInt(), buffer.readInt(), buffer.readInt());
                break;
            case SLOT:
                data = buffer.readSlot();
                break;
            case ROTATION:
                data = new EntityRotation(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                break;
            case POSITION:
                data = buffer.readPosition();
                break;
            case OPT_CHAT:
                if (buffer.readBoolean()) {
                    data = buffer.readTextComponent();
                }
                break;
            case OPT_POSITION:
                if (buffer.readBoolean()) {
                    data = buffer.readPosition();
                }
                break;
            case DIRECTION:
                data = buffer.readDirection();
                break;
            case OPT_UUID:
                if (buffer.readBoolean()) {
                    data = buffer.readUUID();
                }
                break;
            case NBT:
                data = buffer.readNBT();
                break;
            case PARTICLE:
                data = buffer.readParticle();
                break;
            case POSE:
                data = buffer.readPose();
                break;
            case BLOCK_ID:
                int blockId = buffer.readVarInt();
                data = Blocks.byId(blockId >> 4, blockId & 0xF);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return data;
    }

    public HashMap<Integer, MetaDataSet> getSets() {
        return sets;
    }

    public boolean onFire() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(0).getData(), 0x01);
        }
        return false;
    }

    public boolean isSneaking() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(0).getData(), 0x02);
        }
        return false;
    }

    public boolean isSprinting() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(0).getData(), 0x08);
        }
        return false;
    }

    public boolean isEating() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
                return BitByte.isBitMask((byte) sets.get(0).getData(), 0x10);
        }
        return false;
    }

    public boolean isSwimming() {
        switch (version) {
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(0).getData(), 0x10);
        }
        return false;
    }

    public boolean isDrinking() {
        return isEating();
    }

    public boolean isBlocking() {
        return isEating();
    }

    public boolean isInvisible() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 0x20);
        }
        return false;
    }

    public boolean isGlowing() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 0x40);
        }
        return false;
    }

    public boolean isFlyingWithElytra() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 0x80);
        }
        return false;
    }

    public TextComponent getNameTag() {
        switch (version) {
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return new TextComponent((String) sets.get(2).getData());
            case VERSION_1_13_2:
                return (TextComponent) sets.get(2).getData();
        }
        return null;
    }

    public boolean isCustomNameVisible() {
        switch (version) {
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (boolean) sets.get(3).getData();
        }
        return false;
    }

    public boolean isSilent() {
        switch (version) {
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (boolean) sets.get(4).getData();
        }
        return false;
    }

    public boolean hasGravity() {
        switch (version) {
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return !(boolean) sets.get(5).getData();
        }
        return true;
    }

    public enum Types {
        BYTE(0),
        SHORT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 1000)}), // got removed in 1.9
        INT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 1001)}),
        VAR_INT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 1)}),
        FLOAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 3), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 2)}),
        STRING(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 4), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 3)}),
        CHAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 4)}),
        OPT_CHAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 5)}),
        SLOT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 5), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 6)}),
        BOOLEAN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 6), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 7)}),
        VECTOR(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 6), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 1002)}),
        ROTATION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 7), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 8)}),
        POSITION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 8), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 9)}),
        OPT_POSITION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 9), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 10)}),
        DIRECTION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 10), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 11)}),
        OPT_UUID(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 11), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 12)}),
        BLOCK_ID(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 12), new MapSet<>(ProtocolVersion.VERSION_1_10, 1003)}),
        OPT_BLOCK_ID(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 12), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 13)}),
        NBT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_12_2, 13), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 14)}),
        PARTICLE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 15)}),
        VILLAGER_DATA(-1),
        OPT_VAR_INT(-1),
        POSE(-1);

        final VersionValueMap<Integer> valueMap;

        Types(MapSet<ProtocolVersion, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        Types(int id) {
            valueMap = new VersionValueMap<>(id);
        }

        public static Types byId(int id, ProtocolVersion version) {
            for (Types types : values()) {
                if (types.getId(version) == id) {
                    return types;
                }
            }
            return null;
        }

        public int getId(ProtocolVersion version) {
            Integer ret = valueMap.get(version);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }

    public static class MetaDataSet {
        final int index;
        final Object data;

        public MetaDataSet(int index, Object data) {
            this.index = index;
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public int getIndex() {
            return index;
        }
    }
}
