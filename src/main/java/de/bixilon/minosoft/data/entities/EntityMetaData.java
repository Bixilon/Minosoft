/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities;

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.MapSet;
import de.bixilon.minosoft.data.Vector;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.UUID;

public class EntityMetaData {

    final MetaDataHashMap sets = new MetaDataHashMap();
    final Connection connection;

    public EntityMetaData(Connection connection) {
        this.connection = connection;
    }

    public static Object getData(EntityMetaDataValueTypes type, InByteBuffer buffer) {
        return switch (type) {
            case BYTE -> buffer.readByte();
            case VAR_INT -> buffer.readVarInt();
            case SHORT -> buffer.readUnsignedShort();
            case INT -> buffer.readInt();
            case FLOAT -> buffer.readFloat();
            case STRING -> buffer.readString();
            case CHAT -> buffer.readTextComponent();
            case BOOLEAN -> buffer.readBoolean();
            case VECTOR -> new Vector(buffer.readInt(), buffer.readInt(), buffer.readInt());
            case SLOT -> buffer.readSlot();
            case ROTATION -> new EntityRotation(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            case POSITION -> buffer.readPosition();
            case OPT_CHAT -> {
                if (buffer.readBoolean()) {
                    yield buffer.readTextComponent();
                }
                yield null;
            }
            case OPT_POSITION -> {
                if (buffer.readBoolean()) {
                    yield buffer.readPosition();
                }
                yield null;
            }
            case DIRECTION -> buffer.readDirection();
            case OPT_UUID -> {
                if (buffer.readBoolean()) {
                    yield buffer.readUUID();
                }
                yield null;
            }
            case NBT -> buffer.readNBT();
            case PARTICLE -> buffer.readParticle();
            case POSE -> buffer.readPose();
            case BLOCK_ID -> buffer.getConnection().getMapping().getBlockById(buffer.readVarInt());
            case OPT_VAR_INT -> buffer.readVarInt() - 1;
            case VILLAGER_DATA -> new VillagerData(VillagerData.VillagerTypes.byId(buffer.readVarInt()), VillagerData.VillagerProfessions.byId(buffer.readVarInt(), buffer.getVersionId()), VillagerData.VillagerLevels.byId(buffer.readVarInt()));
            case OPT_BLOCK_ID -> {
                int blockId = buffer.readVarInt();
                if (blockId == 0) {
                    yield null;
                }
                yield buffer.getConnection().getMapping().getBlockById(blockId);
            }
        };
    }

    public MetaDataHashMap getSets() {
        return sets;
    }

    public enum EntityMetaDataValueTypes {
        BYTE(0),
        SHORT(new MapSet[]{new MapSet<>(0, 1), new MapSet<>(57, -1)}), // got removed in 1.9
        INT(new MapSet[]{new MapSet<>(0, 2), new MapSet<>(57, -1)}),
        VAR_INT(new MapSet[]{new MapSet<>(57, 1)}),
        FLOAT(new MapSet[]{new MapSet<>(0, 3), new MapSet<>(57, 2)}),
        STRING(new MapSet[]{new MapSet<>(0, 4), new MapSet<>(57, 3)}),
        CHAT(new MapSet[]{new MapSet<>(57, 4)}),
        OPT_CHAT(new MapSet[]{new MapSet<>(346, 5)}), // ToDo: when where the 1.13 changes? in 346?
        SLOT(new MapSet[]{new MapSet<>(0, 5), new MapSet<>(346, 6)}),
        BOOLEAN(new MapSet[]{new MapSet<>(57, 6), new MapSet<>(346, 7)}),
        VECTOR(new MapSet[]{new MapSet<>(0, 6), new MapSet<>(57, -1)}),
        ROTATION(new MapSet[]{new MapSet<>(44, 7), new MapSet<>(346, 8)}),
        POSITION(new MapSet[]{new MapSet<>(57, 8), new MapSet<>(346, 9)}),
        OPT_POSITION(new MapSet[]{new MapSet<>(57, 9), new MapSet<>(346, 10)}),
        DIRECTION(new MapSet[]{new MapSet<>(57, 10), new MapSet<>(346, 11)}),
        OPT_UUID(new MapSet[]{new MapSet<>(57, 11), new MapSet<>(346, 12)}),
        BLOCK_ID(new MapSet[]{new MapSet<>(67, 12), new MapSet<>(210, -1)}), // ToDo: test: 1.10 blockId replacement
        OPT_BLOCK_ID(new MapSet[]{new MapSet<>(210, 12), new MapSet<>(346, 13)}),
        NBT(new MapSet[]{new MapSet<>(318, 13), new MapSet<>(346, 14)}),
        PARTICLE(new MapSet[]{new MapSet<>(346, 15)}),
        VILLAGER_DATA(new MapSet[]{new MapSet<>(451, 16)}),
        OPT_VAR_INT(new MapSet[]{new MapSet<>(459, 17)}),
        POSE(new MapSet[]{new MapSet<>(461, 18)});

        final VersionValueMap<Integer> valueMap;

        EntityMetaDataValueTypes(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        EntityMetaDataValueTypes(int id) {
            valueMap = new VersionValueMap<>(id);
        }

        public static EntityMetaDataValueTypes byId(int id, int versionId) {
            for (EntityMetaDataValueTypes types : values()) {
                if (types.getId(versionId) == id) {
                    return types;
                }
            }
            return null;
        }

        public int getId(Integer versionId) {
            Integer ret = valueMap.get(versionId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }

    public class MetaDataHashMap extends HashMap<Integer, Object> {

        public Poses getPose(EntityMetaDataFields field) {
            return get(field);
        }

        public byte getByte(EntityMetaDataFields field) {
            return get(field);
        }

        public VillagerData getVillagerData(EntityMetaDataFields field) {
            return get(field);
        }

        public ParticleData getParticle(EntityMetaDataFields field) {
            return get(field);
        }

        public CompoundTag getNBT(EntityMetaDataFields field) {
            return get(field);
        }

        public Block getBlock(EntityMetaDataFields field) {
            return get(field);
        }

        public UUID getUUID(EntityMetaDataFields field) {
            return get(field);
        }

        public Directions getDirection(EntityMetaDataFields field) {
            return get(field);
        }

        public BlockPosition getPosition(EntityMetaDataFields field) {
            return get(field);
        }

        public EntityRotation getRotation(EntityMetaDataFields field) {
            return get(field);
        }

        public Vector getVector(EntityMetaDataFields field) {
            return get(field);
        }

        public boolean getBoolean(EntityMetaDataFields field) {
            Object ret = get(field);
            if (ret instanceof Byte b) {
                return b == 0x01;
            }
            return (boolean) ret;
        }

        public boolean getBitMask(EntityMetaDataFields field, int bitMask) {
            return BitByte.isBitMask(getByte(field), bitMask);
        }

        @SuppressWarnings("unchecked")
        public <K> K get(EntityMetaDataFields field) {
            Integer index = connection.getMapping().getEntityMetaDataIndex(field);
            if (index == null) {
                // ups, index not found. Index not available in this version?, mappings broken or mappings not available
                return field.getDefaultValue();
            }
            if (containsKey(index)) {
                Object ret = super.get(index);
                try {
                    return (K) ret;
                } catch (ClassCastException e) {
                    Log.printException(e, LogLevels.VERBOSE);
                }
            }
            return field.getDefaultValue();
        }

        public Slot getSlot(EntityMetaDataFields field) {
            return get(field);
        }

        public ChatComponent getChatComponent(EntityMetaDataFields field) {
            Object object = get(field);
            if (object instanceof String string) {
                return ChatComponent.fromString(string);
            }
            return (ChatComponent) object;
        }

        public String getString(EntityMetaDataFields field) {
            return get(field);
        }

        public float getFloat(EntityMetaDataFields field) {
            return get(field);
        }

        public int getInt(EntityMetaDataFields field) {
            Object value = get(field);
            if (value instanceof Byte b) {
                return b;
            }
            return (int) value;
        }

        public Short getShort(EntityMetaDataFields field) {
            return get(field);
        }
    }

}
