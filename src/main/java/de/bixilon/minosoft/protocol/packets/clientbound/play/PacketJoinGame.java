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
import de.bixilon.minosoft.game.datatypes.Dimension;
import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.LevelType;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

public class PacketJoinGame implements ClientboundPacket {
    int entityId;
    boolean hardcore;
    GameMode gameMode;
    Dimension dimension;
    Difficulty difficulty;
    int maxPlayers;
    LevelType levelType;
    boolean reducedDebugScreen;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimension.byId(buffer.readByte());
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
            case VERSION_1_12_2: {
                this.entityId = buffer.readInt();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimension.byId(buffer.readInt());
                difficulty = Difficulty.byId(buffer.readByte());
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                reducedDebugScreen = buffer.readBoolean();
                return true;
            }
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving join game packet (entityId=%s, gameMode=%s, dimension=%s, difficulty=%s, hardcore=%s)", entityId, gameMode.name(), dimension.name(), difficulty.name(), hardcore));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
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
}
