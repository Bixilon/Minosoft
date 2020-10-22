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

import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.LevelTypes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketRespawn implements ClientboundPacket {
    Dimension dimension;
    Difficulties difficulty;
    GameModes gameMode;
    LevelTypes levelType;
    long hashedSeed;
    boolean isDebug = false;
    boolean isFlat = false;
    boolean copyMetaData = false;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 718) {
            if (buffer.getVersionId() < 108) {
                dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readByte());
            } else {
                dimension = buffer.getConnection().getMapping().getDimensionById(buffer.readInt());
            }
        } else {
            dimension = buffer.getConnection().getMapping().getDimensionByIdentifier(buffer.readString());
        }
        if (buffer.getVersionId() < 464) {
            difficulty = Difficulties.byId(buffer.readByte());
        }

        if (buffer.getVersionId() >= 719) {
            buffer.readString(); // world
        }
        if (buffer.getVersionId() >= 552) {
            hashedSeed = buffer.readLong();
        }
        gameMode = GameModes.byId(buffer.readByte());

        if (buffer.getVersionId() >= 730) {
            buffer.readByte(); // previous game mode
        }
        if (buffer.getVersionId() >= 1 && buffer.getVersionId() < 716) {
            levelType = LevelTypes.byType(buffer.readString());
        }
        if (buffer.getVersionId() >= 716) {
            isDebug = buffer.readBoolean();
            isFlat = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= 714) {
            copyMetaData = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Respawn packet received (dimension=%s, difficulty=%s, gamemode=%s, levelType=%s)", dimension, difficulty, gameMode, levelType));
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Difficulties getDifficulty() {
        return difficulty;
    }

    public GameModes getGameMode() {
        return gameMode;
    }

    public LevelTypes getLevelType() {
        return levelType;
    }
}
