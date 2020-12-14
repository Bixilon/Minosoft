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
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.JoinGameEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.ListTag;
import de.bixilon.minosoft.util.nbt.tag.NBTTag;

import java.util.HashMap;

public class PacketJoinGame extends ClientboundPacket {
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
            this.hardcore = BitByte.isBitSet(gameModeRaw, 3);
            // remove hardcore bit and get gamemode
            gameModeRaw &= ~0x8;
            this.gameMode = GameModes.byId(gameModeRaw);

            if (buffer.getVersionId() < 108) {
                this.dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readByte());
            } else {
                this.dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
            }
            this.difficulty = Difficulties.byId(buffer.readUnsignedByte());
            this.maxPlayers = buffer.readByte();
            if (buffer.getVersionId() >= 1) {
                this.levelType = LevelTypes.byType(buffer.readString());
            }

            if (buffer.getVersionId() < 29) {
                return true;
            }
            this.reducedDebugScreen = buffer.readBoolean();
            return true;
        }
        this.entityId = buffer.readInt();
        if (buffer.getVersionId() < 738) {
            byte gameModeRaw = buffer.readByte();
            this.hardcore = BitByte.isBitSet(gameModeRaw, 3);
            // remove hardcore bit and get gamemode
            gameModeRaw &= ~0x8;
            this.gameMode = GameModes.byId(gameModeRaw);
        } else {
            this.hardcore = buffer.readBoolean();
            this.gameMode = GameModes.byId(buffer.readUnsignedByte());
        }
        if (buffer.getVersionId() >= 730) {
            buffer.readByte(); // previous game mode
        }
        if (buffer.getVersionId() >= 719) {
            String[] worlds = buffer.readStringArray(buffer.readVarInt());
        }
        if (buffer.getVersionId() < 718) {
            this.dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
        } else {
            NBTTag dimensionCodec = buffer.readNBT();
            this.dimensions = parseDimensionCodec(dimensionCodec, buffer.getVersionId());
            if (buffer.getVersionId() < 748) {
                String[] currentDimensionSplit = buffer.readString().split(":", 2);
                this.dimension = this.dimensions.get(currentDimensionSplit[0]).get(currentDimensionSplit[1]);
            } else {
                CompoundTag tag = (CompoundTag) buffer.readNBT();
                if (tag.getByteTag("has_skylight").getValue() == 0x01) { // ToDo: this is just for not messing up the skylight
                    this.dimension = this.dimensions.get(ProtocolDefinition.DEFAULT_MOD).get("overworld");
                } else {
                    this.dimension = this.dimensions.get(ProtocolDefinition.DEFAULT_MOD).get("the_nether");
                }
            }
        }

        if (buffer.getVersionId() >= 719) {
            buffer.readString(); // world
        }
        if (buffer.getVersionId() >= 552) {
            this.hashedSeed = buffer.readLong();
        }
        if (buffer.getVersionId() < 464) {
            this.difficulty = Difficulties.byId(buffer.readUnsignedByte());
        }
        if (buffer.getVersionId() < 749) {
            this.maxPlayers = buffer.readByte();
        } else {
            this.maxPlayers = buffer.readVarInt();
        }
        if (buffer.getVersionId() < 716) {
            this.levelType = LevelTypes.byType(buffer.readString());
        }
        if (buffer.getVersionId() >= 468) {
            this.viewDistance = buffer.readVarInt();
        }
        if (buffer.getVersionId() >= 716) {
            boolean isDebug = buffer.readBoolean();
            if (buffer.readBoolean()) {
                this.levelType = LevelTypes.FLAT;
            }
        }
        this.reducedDebugScreen = buffer.readBoolean();
        if (buffer.getVersionId() >= 552) {
            this.enableRespawnScreen = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new JoinGameEvent(connection, this))) {
            return;
        }

        connection.getPlayer().setGameMode(getGameMode());
        connection.getPlayer().getWorld().setHardcore(isHardcore());
        connection.getMapping().setDimensions(getDimensions());
        connection.getPlayer().getWorld().setDimension(getDimension());
        PlayerEntity entity = new PlayerEntity(connection, getEntityId(), connection.getPlayer().getPlayerUUID(), null, null, connection.getPlayer().getPlayerName(), null, null);
        connection.getPlayer().setEntity(entity);
        connection.getPlayer().getWorld().addEntity(entity);
        connection.getSender().sendChatMessage("I am alive! ~ Minosoft");
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
        Log.protocol(String.format("[IN] Receiving join game packet (entityId=%s, gameMode=%s, dimension=%s, difficulty=%s, hardcore=%s, viewDistance=%d)", this.entityId, this.gameMode, this.dimension, this.difficulty, this.hardcore, this.viewDistance));
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public GameModes getGameMode() {
        return this.gameMode;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public LevelTypes getLevelType() {
        return this.levelType;
    }

    public Difficulties getDifficulty() {
        return this.difficulty;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public HashMap<String, HashBiMap<String, Dimension>> getDimensions() {
        return this.dimensions;
    }

    public boolean isReducedDebugScreen() {
        return this.reducedDebugScreen;
    }

    public boolean isEnableRespawnScreen() {
        return this.enableRespawnScreen;
    }

    public long getHashedSeed() {
        return this.hashedSeed;
    }
}
