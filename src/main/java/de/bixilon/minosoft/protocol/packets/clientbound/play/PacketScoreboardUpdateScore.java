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
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardUpdateScore implements ClientboundPacket {
    String itemName;
    ScoreboardUpdateScoreAction action;
    String scoreName;
    int scoreValue;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                itemName = buffer.readString();
                action = ScoreboardUpdateScoreAction.byId(buffer.readByte());
                if (action == ScoreboardUpdateScoreAction.REMOVE) {
                    return true;
                }
                // not present id action == REMOVE
                scoreName = buffer.readString();
                scoreValue = buffer.readInt();
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                itemName = buffer.readString();
                action = ScoreboardUpdateScoreAction.byId(buffer.readByte());
                scoreName = buffer.readString();

                if (action == ScoreboardUpdateScoreAction.REMOVE) {
                    break;
                }
                // not present id action == REMOVE
                scoreValue = buffer.readVarInt();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received scoreboard score update (itemName=\"%s\", action=%s, scoreName=\"%s\", scoreValue=%d", itemName, action.name(), scoreName, scoreValue));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public String getItemName() {
        return itemName;
    }

    public ScoreboardUpdateScoreAction getAction() {
        return action;
    }

    public String getScoreName() {
        return scoreName;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public enum ScoreboardUpdateScoreAction {
        CREATE_UPDATE(0),
        REMOVE(1);

        final int id;

        ScoreboardUpdateScoreAction(int id) {
            this.id = id;
        }

        public static ScoreboardUpdateScoreAction byId(int id) {
            for (ScoreboardUpdateScoreAction a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
