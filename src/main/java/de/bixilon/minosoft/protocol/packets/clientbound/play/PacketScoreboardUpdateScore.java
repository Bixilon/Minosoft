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

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective;
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketScoreboardUpdateScore extends ClientboundPacket {
    private final String itemName;
    private final ScoreboardUpdateScoreActions action;
    private String scoreName;
    private int scoreValue;

    public PacketScoreboardUpdateScore(InByteBuffer buffer) {
        this.itemName = buffer.readString();
        this.action = ScoreboardUpdateScoreActions.byId(buffer.readUnsignedByte());
        if (buffer.getVersionId() < V_14W04A) { // ToDo
            if (this.action == ScoreboardUpdateScoreActions.REMOVE) {
                return;
            }
            // not present id action == REMOVE
            this.scoreName = buffer.readString();
            this.scoreValue = buffer.readInt();
            return;
        }
        this.scoreName = buffer.readString();

        if (this.action == ScoreboardUpdateScoreActions.REMOVE) {
            return;
        }
        // not present id action == REMOVE
        this.scoreValue = buffer.readVarInt();
    }

    @Override
    public void handle(Connection connection) {
        switch (getAction()) {
            case CREATE_UPDATE -> connection.getPlayer().getScoreboardManager().getObjective(getScoreName()).addScore(new ScoreboardScore(getItemName(), getScoreName(), getScoreValue()));
            case REMOVE -> {
                ScoreboardObjective objective = connection.getPlayer().getScoreboardManager().getObjective(getScoreName());
                if (objective != null) {
                    // thanks mojang
                    objective.removeScore(getItemName());
                }
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received scoreboard score update (itemName=\"%s\", action=%s, scoreName=\"%s\", scoreValue=%d", this.itemName, this.action, this.scoreName, this.scoreValue));
    }

    public String getItemName() {
        return this.itemName;
    }

    public ScoreboardUpdateScoreActions getAction() {
        return this.action;
    }

    public String getScoreName() {
        return this.scoreName;
    }

    public int getScoreValue() {
        return this.scoreValue;
    }

    public enum ScoreboardUpdateScoreActions {
        CREATE_UPDATE,
        REMOVE;

        private static final ScoreboardUpdateScoreActions[] SCOREBOARD_UPDATE_SCORE_ACTIONS = values();

        public static ScoreboardUpdateScoreActions byId(int id) {
            return SCOREBOARD_UPDATE_SCORE_ACTIONS[id];
        }
    }
}
