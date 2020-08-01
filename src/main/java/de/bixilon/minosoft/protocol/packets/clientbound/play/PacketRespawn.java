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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketRespawn implements ClientboundPacket {
    Dimension dimension;
    Difficulty difficulty;
    GameMode gameMode;
    LevelType levelType;
    long hashedSeed;
    boolean isDebug = false;
    boolean isFlat = false;
    boolean copyMetaData = false;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 743) { //ToDo
            dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
            if (buffer.getProtocolId() < 464) {
                difficulty = Difficulty.byId(buffer.readByte());
            }
            if (buffer.getProtocolId() >= 552) {
                hashedSeed = buffer.readLong();
            }
            gameMode = GameMode.byId(buffer.readByte());
            if (buffer.getProtocolId() >= 1) {
                levelType = LevelType.byType(buffer.readString());
            }
            return true;
        }
        dimension = buffer.getConnection().getMapping().getDimensionByIdentifier(buffer.readString());
        buffer.readString(); // world
        hashedSeed = buffer.readLong();
        gameMode = GameMode.byId(buffer.readByte());
        buffer.readByte(); // previous game mode
        isDebug = buffer.readBoolean();
        isFlat = buffer.readBoolean();
        copyMetaData = buffer.readBoolean();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Respawn packet received (dimension=%s, difficulty=%s, gamemode=%s, levelType=%s)", dimension, difficulty, gameMode, levelType));
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
