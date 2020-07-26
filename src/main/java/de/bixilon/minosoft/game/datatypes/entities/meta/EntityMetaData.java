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
import de.bixilon.minosoft.game.datatypes.entities.Pose;
import de.bixilon.minosoft.game.datatypes.entities.VillagerData;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.particle.Particle;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;


public class EntityMetaData {

    final MetaDataHashMap sets;
    final ProtocolVersion version;

    /*
    1.7.10: https://wiki.vg/index.php?title=Entity_metadata&oldid=5991
    1.8: https://wiki.vg/index.php?title=Entity_metadata&oldid=6611
    1.9.4: https://wiki.vg/index.php?title=Entity_metadata&oldid=7955
    1.10: https://wiki.vg/index.php?title=Entity_metadata&oldid=8241
    1.11: https://wiki.vg/index.php?title=Entity_metadata&oldid=8413
    1.12: https://wiki.vg/index.php?title=Entity_metadata&oldid=13919
    1.13: https://wiki.vg/index.php?title=Entity_metadata&oldid=14800
    1.14.4: https://wiki.vg/index.php?title=Entity_metadata&oldid=15239
    1.15: https://wiki.vg/index.php?title=Entity_metadata&oldid=15885
     */

    public EntityMetaData(MetaDataHashMap sets, ProtocolVersion version) {
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
                data = Blocks.getBlock(blockId, buffer.getVersion());
                break;
            case OPT_VAR_INT:
                data = buffer.readVarInt() - 1;
                break;
            case VILLAGER_DATA:
                data = new VillagerData(VillagerData.VillagerTypes.byId(buffer.readVarInt()), VillagerData.VillagerProfessions.byId(buffer.readVarInt(), buffer.getVersion()), VillagerData.VillagerLevels.byId(buffer.readVarInt()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return data;
    }

    public MetaDataHashMap getSets() {
        return sets;
    }

    public boolean onFire() {
        return sets.getBitMask(0, 0x01, false);
    }

    private boolean isSneaking() {
        return sets.getBitMask(0, 0x02, false);
    }

    public boolean isSprinting() {
        return sets.getBitMask(0, 0x08, false);
    }

    public boolean isEating() {
        if (version.getVersionNumber() > ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return false;
        }
        return sets.getBitMask(0, 0x10, false);
    }

    private boolean isSwimming() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return false;
        }
        return sets.getBitMask(0, 0x10, false);
    }

    public boolean isDrinking() {
        return isEating();
    }

    public boolean isBlocking() {
        return isEating();
    }

    public boolean isInvisible() {
        return sets.getBitMask(0, 0x20, false);
    }

    public boolean isGlowing() {
        return sets.getBitMask(0, 0x40, false);
    }

    public boolean isFlyingWithElytra() {
        return sets.getBitMask(0, 0x80, false);
    }

    @Nullable
    public TextComponent getNameTag() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return null;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return new TextComponent(sets.getString(2, null));
        }
        return sets.getTextComponent(2, null);
    }

    public boolean isCustomNameVisible() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return false;
        }
        return sets.getBoolean(3, false);
    }

    public boolean isSilent() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return false;
        }
        return sets.getBoolean(4, false);

    }

    public boolean hasGravity() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return true;
        }
        return !sets.getBoolean(5, false);
    }

    public Pose getPose() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            if (isSneaking()) {
                return Pose.SNEAKING;
            } else if (isSwimming()) {
                return Pose.SWIMMING;
            } else {
                return Pose.STANDING;
            }
        }
        return sets.getPose(6, Pose.STANDING);
    }

    protected int getLastDataIndex() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            throw new IllegalArgumentException("EntityMetaData::getLastDataIndex does not work below 1.9!");
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return 4;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return 5;
        }
        return 6;
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
        VILLAGER_DATA(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_14_4, 16)}),
        OPT_VAR_INT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_14_4, 17)}),
        POSE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_14_4, 18)});

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

    public static class MetaDataHashMap extends HashMap<Integer, Object> {
        public Pose getPose(int index, Pose defaultValue) {
            return (Pose) get(index, defaultValue);
        }

        public VillagerData getVillagerData(int index, VillagerData defaultValue) {
            return (VillagerData) get(index, defaultValue);
        }

        public Particle getParticle(int index, Particle defaultValue) {
            return (Particle) get(index, defaultValue);
        }

        public CompoundTag getNBT(int index, CompoundTag defaultValue) {
            return (CompoundTag) get(index, defaultValue);
        }

        public Block getBlock(int index, Block defaultValue) {
            return (Block) get(index, defaultValue);
        }

        public UUID getUUID(int index, UUID defaultValue) {
            return (UUID) get(index, defaultValue);
        }

        public Direction getDirection(int index, Direction defaultValue) {
            return (Direction) get(index, defaultValue);
        }

        public BlockPosition getPosition(int index, BlockPosition defaultValue) {
            return (BlockPosition) get(index, defaultValue);
        }

        public EntityRotation getRotation(int index, EntityRotation defaultValue) {
            return (EntityRotation) get(index, defaultValue);
        }

        public Vector getVector(int index, Vector defaultValue) {
            return (Vector) get(index, defaultValue);
        }

        public boolean getBoolean(int index, boolean defaultValue) {
            Object ret = get(index, defaultValue);
            if (ret instanceof Byte) {
                return (byte) ret == 0x01;
            }
            return (boolean) get(index, defaultValue);
        }

        public boolean getBitMask(int index, int bitMask, boolean defaultValue) {
            return BitByte.isBitMask(getByte(index, (defaultValue ? 1 : 0)), bitMask);
        }

        public Slot getSlot(int index, Slot defaultValue) {
            return (Slot) get(index, defaultValue);
        }

        public TextComponent getTextComponent(int index, TextComponent defaultValue) {
            return (TextComponent) get(index, defaultValue);
        }

        public String getString(int index, String defaultValue) {
            return (String) get(index, defaultValue);
        }

        public float getFloat(int index, float defaultValue) {
            return (float) get(index, defaultValue);
        }

        public int getInt(int index, int defaultValue) {
            return (int) get(index, defaultValue);
        }

        public short getShort(int index, int defaultValue) {
            return (short) get(index, defaultValue);
        }

        public byte getByte(int index, int defaultValue) {
            return (byte) get(index, defaultValue);
        }

        public Object get(int index, Object defaultValue) {
            if (containsKey(index)) {
                return super.get(index);
            }
            return defaultValue;
        }
    }

}
