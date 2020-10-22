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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardUpdateScore implements ClientboundPacket {
    String itemName;
    ScoreboardUpdateScoreActions action;
    String scoreName;
    int scoreValue;

    @Override
    public boolean read(InByteBuffer buffer) {
        itemName = buffer.readString();
        action = ScoreboardUpdateScoreActions.byId(buffer.readByte());
        if (buffer.getVersionId() < 7) { // ToDo
            if (action == ScoreboardUpdateScoreActions.REMOVE) {
                return true;
            }
            // not present id action == REMOVE
            scoreName = buffer.readString();
            scoreValue = buffer.readInt();
            return true;
        }
        scoreName = buffer.readString();

        if (action == ScoreboardUpdateScoreActions.REMOVE) {
            return true;
        }
        // not present id action == REMOVE
        scoreValue = buffer.readVarInt();
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received scoreboard score update (itemName=\"%s\", action=%s, scoreName=\"%s\", scoreValue=%d", itemName, action, scoreName, scoreValue));
    }

    public String getItemName() {
        return itemName;
    }

    public ScoreboardUpdateScoreActions getAction() {
        return action;
    }

    public String getScoreName() {
        return scoreName;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public enum ScoreboardUpdateScoreActions {
        CREATE_UPDATE,
        REMOVE;

        public static ScoreboardUpdateScoreActions byId(int id) {
            return values()[id];
        }
    }
}
