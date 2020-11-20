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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.LevelTypes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.ListTag;
import de.bixilon.minosoft.util.nbt.tag.NBTTag;

import java.util.HashMap;

public class PacketJoinGame implements ClientboundPacket {
    int entityId;
    boolean hardcore;
    GameModes gameMode;
    Dimension dimension;
    Difficulties difficulty;
    int viewDistance = -1;
    int maxPlayers;
    LevelTypes levelType;
    boolean reducedDebugScreen;
    boolean enableRespawnScreen = true;
    long hashedSeed;
    HashMap<String, HashBiMap<String, Dimension>> dimensions;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 108) {
            this.entityId = buffer.readInt();
            byte gameModeRaw = buffer.readByte();
            hardcore = BitByte.isBitSet(gameModeRaw, 3);
            // remove hardcore bit and get gamemode
            gameModeRaw &= ~0x8;
            gameMode = GameModes.byId(gameModeRaw);

            if (buffer.getVersionId() < 108) {
                dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readByte());
            } else {
                dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
            }
            difficulty = Difficulties.byId(buffer.readByte());
            maxPlayers = buffer.readByte();
            if (buffer.getVersionId() >= 1) {
                levelType = LevelTypes.byType(buffer.readString());
            }

            if (buffer.getVersionId() < 29) {
                return true;
            }
            reducedDebugScreen = buffer.readBoolean();
            return true;
        }
        this.entityId = buffer.readInt();
        if (buffer.getVersionId() < 738) {
            byte gameModeRaw = buffer.readByte();
            hardcore = BitByte.isBitSet(gameModeRaw, 3);
            // remove hardcore bit and get gamemode
            gameModeRaw &= ~0x8;
            gameMode = GameModes.byId(gameModeRaw);
        } else {
            hardcore = buffer.readBoolean();
            gameMode = GameModes.byId(buffer.readByte());
        }
        if (buffer.getVersionId() >= 730) {
            buffer.readByte(); // previous game mode
        }
        if (buffer.getVersionId() >= 719) {
            String[] worlds = buffer.readStringArray(buffer.readVarInt());
        }
        if (buffer.getVersionId() < 718) {
            dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
        } else {
            NBTTag dimensionCodec = buffer.readNBT();
            dimensions = parseDimensionCodec(dimensionCodec, buffer.getVersionId());
            if (buffer.getVersionId() < 748) {
                String[] currentDimensionSplit = buffer.readString().split(":", 2);
                dimension = dimensions.get(currentDimensionSplit[0]).get(currentDimensionSplit[1]);
            } else {
                CompoundTag tag = (CompoundTag) buffer.readNBT();
                if (tag.getByteTag("has_skylight").getValue() == 0x01) { //ToDo: this is just for not messing up the skylight
                    dimension = dimensions.get(ProtocolDefinition.DEFAULT_MOD).get("overworld");
                } else {
                    dimension = dimensions.get(ProtocolDefinition.DEFAULT_MOD).get("the_nether");
                }
            }
        }

        if (buffer.getVersionId() >= 719) {
            buffer.readString(); // world
        }
        if (buffer.getVersionId() >= 552) {
            hashedSeed = buffer.readLong();
        }
        if (buffer.getVersionId() < 464) {
            difficulty = Difficulties.byId(buffer.readByte());
        }
        if (buffer.getVersionId() < 749) {
            maxPlayers = buffer.readByte();
        } else {
            maxPlayers = buffer.readVarInt();
        }
        if (buffer.getVersionId() < 716) {
            levelType = LevelTypes.byType(buffer.readString());
        }
        if (buffer.getVersionId() >= 468) {
            viewDistance = buffer.readVarInt();
        }
        if (buffer.getVersionId() >= 716) {
            boolean isDebug = buffer.readBoolean();
            if (buffer.readBoolean()) {
                levelType = LevelTypes.FLAT;
            }
        }
        reducedDebugScreen = buffer.readBoolean();
        if (buffer.getVersionId() >= 552) {
            enableRespawnScreen = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    private HashMap<String, HashBiMap<String, Dimension>> parseDimensionCodec(NBTTag nbt, int versionId) {
        HashMap<String, HashBiMap<String, Dimension>> dimensionMap = new HashMap<>();
        ListTag listTag;
        if (versionId < 740) {
            listTag = ((CompoundTag) nbt).getListTag("dimension");
        } else {
            listTag = ((CompoundTag) nbt).getCompoundTag("minecraft:dimension_type").getListTag("value");
        }

        listTag.getValue().forEach((tag) -> {
            CompoundTag compoundTag = (CompoundTag) tag;
            String[] name;
            if (versionId < 725) {
                name = compoundTag.getStringTag("key").getValue().split(":", 2);
            } else {
                name = compoundTag.getStringTag("name").getValue().split(":", 2);
            }
            if (!dimensionMap.containsKey(name[0])) {
                dimensionMap.put(name[0], HashBiMap.create());
            }
            boolean hasSkylight;
            if (versionId < 725 || versionId >= 744) {
                hasSkylight = compoundTag.getCompoundTag("element").getByteTag("has_skylight").getValue() == 0x01;
            } else {
                hasSkylight = compoundTag.getByteTag("has_skylight").getValue() == 0x01;
            }
            dimensionMap.get(name[0]).put(name[1], new Dimension(name[0], name[1], hasSkylight));
        });
        return dimensionMap;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving join game packet (entityId=%s, gameMode=%s, dimension=%s, difficulty=%s, hardcore=%s, viewDistance=%d)", entityId, gameMode, dimension, difficulty, hardcore, viewDistance));
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public int getEntityId() {
        return entityId;
    }

    public GameModes getGameMode() {
        return gameMode;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public LevelTypes getLevelType() {
        return levelType;
    }

    public Difficulties getDifficulty() {
        return difficulty;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public HashMap<String, HashBiMap<String, Dimension>> getDimensions() {
        return dimensions;
    }

    public boolean isReducedDebugScreen() {
        return reducedDebugScreen;
    }

    public boolean isEnableRespawnScreen() {
        return enableRespawnScreen;
    }

    public long getHashedSeed() {
        return hashedSeed;
    }
}
