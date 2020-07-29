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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.Difficulty;
import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.LevelType;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimension;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimensions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.nbt.tag.ListTag;
import de.bixilon.minosoft.nbt.tag.NBTTag;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;

public class PacketJoinGame implements ClientboundPacket {
    int entityId;
    boolean hardcore;
    GameMode gameMode;
    Dimension dimension;
    Difficulty difficulty;
    int viewDistance = -1;
    int maxPlayers;
    LevelType levelType;
    boolean reducedDebugScreen;
    boolean enableRespawnScreen = true;
    HashMap<String, HashMap<String, Dimension>> dimensions;

    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimensions.byId(buffer.readInt(), buffer.getVersion());
                difficulty = Difficulty.byId(buffer.readByte());
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                // break here if 1.7.10, because this happened later
                if (buffer.getVersion() == ProtocolVersion.VERSION_1_7_10) {
                    return true;
                }
                reducedDebugScreen = buffer.readBoolean();
                return true;
            }
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimensions.byId(buffer.readInt(), buffer.getVersion());
                difficulty = Difficulty.byId(buffer.readByte());
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                reducedDebugScreen = buffer.readBoolean();
                return true;
            }
            case VERSION_1_14_4: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimensions.byId(buffer.readInt(), buffer.getVersion());
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                viewDistance = buffer.readVarInt();
                reducedDebugScreen = buffer.readBoolean();
                return true;
            }
            case VERSION_1_15_2: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimensions.byId(buffer.readInt(), buffer.getVersion());
                long hashedSeed = buffer.readLong();
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                viewDistance = buffer.readVarInt();
                reducedDebugScreen = buffer.readBoolean();
                enableRespawnScreen = buffer.readBoolean();
                return true;
            }
            default: {
                this.entityId = buffer.readInt();
                hardcore = buffer.readBoolean();
                gameMode = GameMode.byId(buffer.readByte());
                buffer.readByte(); // previous game mode
                // worlds
                String[] worlds = buffer.readStringArray(buffer.readVarInt());
                NBTTag dimensionCodec = buffer.readNBT();
                dimensions = parseDimensionCodec(dimensionCodec);
                String[] currentDimensionSplit = buffer.readString().split(":", 2);
                dimension = dimensions.get(currentDimensionSplit[0]).get(currentDimensionSplit[1]);
                buffer.readString(); // world name
                long hashedSeed = buffer.readLong();
                maxPlayers = buffer.readByte();
                levelType = LevelType.UNKNOWN;
                viewDistance = buffer.readVarInt();
                reducedDebugScreen = buffer.readBoolean();
                enableRespawnScreen = buffer.readBoolean();
                boolean isDebug = buffer.readBoolean();
                if (buffer.readBoolean()) {
                    levelType = LevelType.FLAT;
                }
                return true;
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving join game packet (entityId=%s, gameMode=%s, dimension=%s, difficulty=%s, hardcore=%s, viewDistance=%d)", entityId, gameMode, dimension, difficulty, hardcore, viewDistance));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    private HashMap<String, HashMap<String, Dimension>> parseDimensionCodec(NBTTag nbt) {
        HashMap<String, HashMap<String, Dimension>> dimensionMap = new HashMap<>();
        ListTag listTag = ((CompoundTag) nbt).getCompoundTag("minecraft:dimension_type").getListTag("value");

        for (NBTTag tag : listTag.getValue()) {
            CompoundTag compoundTag = (CompoundTag) tag;
            String[] name = compoundTag.getStringTag("name").getValue().split(":", 2);
            if (!dimensionMap.containsKey(name[0])) {
                dimensionMap.put(name[0], new HashMap<>());
            }
            dimensionMap.get(name[0]).put(name[1], new Dimension(name[0], name[1], compoundTag.getByteTag("has_skylight").getValue() == 0x01));
        }
        return dimensionMap;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public int getEntityId() {
        return entityId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public int getViewDistance() {
        return viewDistance;
    }
}
