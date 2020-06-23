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

import de.bixilon.minosoft.game.datatypes.EntityRotation;
import de.bixilon.minosoft.game.datatypes.Vector;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;

public class EntityMetaData {
    final HashMap<Integer, MetaDataSet> sets = new HashMap<>();
    final ProtocolVersion version;

    /*
    1.7.10: https://wiki.vg/index.php?title=Entity_metadata&oldid=5991
    1.8: https://wiki.vg/index.php?title=Entity_metadata&oldid=6611
     */
    public EntityMetaData(InByteBuffer buffer, ProtocolVersion v) {
        version = v;
        switch (v) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                byte item = buffer.readByte();
                while (item != 0x7F) {
                    byte index = (byte) (item & 0x1F);
                    Object data;
                    TypeLegacy type = TypeLegacy.byId((item & 0xFF) >>> 5);
                    switch (type) {
                        case BYTE:
                            data = buffer.readByte();
                            break;
                        case SHORT:
                            data = buffer.readShort();
                            break;
                        case INT:
                            data = buffer.readInteger();
                            break;
                        case FLOAT:
                            data = buffer.readFloat();
                            break;
                        case STRING:
                            data = buffer.readString();
                            break;
                        case VECTOR:
                            data = new Vector(buffer.readInteger(), buffer.readInteger(), buffer.readInteger());
                            break;
                        case SLOT:
                            data = buffer.readSlot(v);
                            break;
                        case POSITION:
                            data = new EntityRotation(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + type);
                    }
                    sets.put((int) index, new MetaDataSet(index, data));


                    item = buffer.readByte();
                }

                break;
                /*
            case VERSION_1_15_2:
        byte index = buffer.readByte();
            while (index != -1) { // 0xFF
            // still data here
            int id = buffer.readVarInt();
            Type type = Type.byId(id);
            Object data;
            switch (type) {
                case BYTE:
                    data = buffer.readByte();
                    break;
                case VAR_INT:
                case OPT_BLOCK_ID:
                case OPT_VAR_INT:
                    data = buffer.readVarInt();
                    break;
                case FLOAT:
                    data = buffer.readFloat();
                    break;
                case STRING:
                    data = buffer.readString();
                    break;
                case CHAT:
                    data = buffer.readChatComponent();
                    break;
                case OPT_CHAT:
                    if (buffer.readBoolean()) {
                        data = buffer.readChatComponent();
                    }
                    break;
                case SLOT:
                    data = buffer.readSlot();
                    break;
                case BOOLEAN:
                    data = buffer.readBoolean();
                    break;
                case ROTATION:
                    //ToDo
                    buffer.readFloat();
                    buffer.readFloat();
                    buffer.readFloat();
                    break;
                case POSITION:
                    data = buffer.readPosition();
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

            }


            index = buffer.readByte();
        }
                 */
        }


    }

    public HashMap<Integer, MetaDataSet> getSets() {
        return sets;
    }

    public boolean onFire() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 0);
        }
        return false;
    }

    public boolean isSneaking() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 1);
        }
        return false;
    }

    public boolean isSprinting() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 2);
        }
        return false;
    }

    public boolean isEating() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(0).getData(), 3);
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
                return BitByte.isBitSet((byte) sets.get(0).getData(), 4);
        }
        return false;
    }

    enum Type implements Types {
        BYTE(0),
        VAR_INT(1),
        FLOAT(2),
        STRING(3),
        CHAT(4),
        OPT_CHAT(5),
        SLOT(6),
        BOOLEAN(7),
        ROTATION(8),
        POSITION(9),
        OPT_POSITION(10),
        DIRECTION(11),
        OPT_UUID(12),
        OPT_BLOCK_ID(13),
        NBT(13),
        PARTICLE(14),
        VILLAGER_DATA(15),
        OPT_VAR_INT(17),
        POSE(18);


        final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type byId(int id) {
            for (Type s : values()) {
                if (s.getId() == id) {
                    return s;
                }
            }
            return null;
        }


        public int getId() {
            return id;
        }
    }

    enum TypeLegacy implements Types {
        BYTE(0),
        SHORT(1),
        INT(2),
        FLOAT(3),
        STRING(4),
        SLOT(5),
        VECTOR(6),
        POSITION(7);


        final int id;

        TypeLegacy(int id) {
            this.id = id;
        }

        public static TypeLegacy byId(int id) {
            for (TypeLegacy s : values()) {
                if (s.getId() == id) {
                    return s;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    interface Types {
        int getId();
    }

    public class MetaDataSet {
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
